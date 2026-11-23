package com.sanchari.dispatch.service;

import com.sanchari.common.event.EventPublisher;
import com.sanchari.common.event.RideOfferEvent;
import com.sanchari.common.event.Topics;
import com.sanchari.dispatch.config.DispatchProperties;
import com.sanchari.location.dto.NearbyDriverView;
import com.sanchari.location.service.LocationService;
import com.sanchari.ride.domain.OfferStatus;
import com.sanchari.ride.domain.Ride;
import com.sanchari.ride.domain.RideOffer;
import com.sanchari.ride.domain.RideStatus;
import com.sanchari.ride.kafka.RideEventProducer;
import com.sanchari.ride.repository.RideOfferRepository;
import com.sanchari.ride.repository.RideRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Expanding-radius dispatch. One call to process() advances a SEARCHING ride one step:
 * <ol>
 *   <li>expire offers older than the TTL;</li>
 *   <li>give up (NO_DRIVER) once the search timeout passes;</li>
 *   <li>wait while any offer is still open;</li>
 *   <li>otherwise offer the ride to the nearest not-yet-offered drivers inside the current radius, and if there are
 *       none, widen the radius by one step (up to the maximum) and try again.</li>
 * </ol>
 * The ride row is locked, so the Kafka consumer and the scheduler never process the same ride concurrently.
 */
@Service
public class DispatchService {

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);

    private final RideRepository rides;
    private final RideOfferRepository offers;
    private final LocationService location;
    private final EventPublisher events;
    private final RideEventProducer rideEvents;
    private final OfferService offerService;
    private final DispatchProperties props;

    public DispatchService(RideRepository rides, RideOfferRepository offers, LocationService location,
                           EventPublisher events, RideEventProducer rideEvents, OfferService offerService,
                           DispatchProperties props) {
        this.rides = rides;
        this.offers = offers;
        this.location = location;
        this.events = events;
        this.rideEvents = rideEvents;
        this.offerService = offerService;
        this.props = props;
    }

    @Transactional
    public void process(UUID rideId) {
        Ride ride = rides.findByIdForUpdate(rideId).orElse(null);
        if (ride == null || ride.getStatus() != RideStatus.SEARCHING) {
            return;
        }
        Instant now = Instant.now();

        offers.expireStale(rideId, now.minusSeconds(props.offerTtlSeconds()));

        if (ride.getCreatedAt().plusSeconds(props.searchTimeoutSeconds()).isBefore(now)) {
            giveUp(ride);
            return;
        }
        if (offers.countByRideIdAndStatus(rideId, OfferStatus.SENT) > 0) {
            return;
        }

        int offered = offerRound(ride);
        while (offered == 0 && ride.getSearchRadiusM() < props.maxRadiusM()) {
            int next = Math.min(ride.getSearchRadiusM() + props.stepM(), props.maxRadiusM());
            ride.setSearchRadiusM(next);
            log.debug("Ride {}: no new drivers, widening radius to {} m", rideId, next);
            offered = offerRound(ride);
        }
        // Nobody left even at the maximum radius: keep SEARCHING; drivers may come online before the timeout.
    }

    private int offerRound(Ride ride) {
        List<NearbyDriverView> drivers = location.findNearbyAvailable(ride.getPickupLat(), ride.getPickupLng(),
                ride.getSearchRadiusM(), ride.getVehicleType().name(), ride.getId(), props.batchSize());
        Instant expiresAt = Instant.now().plusSeconds(props.offerTtlSeconds());
        for (NearbyDriverView d : drivers) {
            offers.save(new RideOffer(ride.getId(), d.getDriverId(), ride.getSearchRadiusM()));
            events.publishAfterCommit(Topics.RIDE_OFFER, d.getDriverId().toString(), "RIDE_OFFER",
                    new RideOfferEvent(ride.getId(), d.getDriverId(), ride.getSearchRadiusM(), expiresAt,
                            ride.getPickupAddress(), ride.getPickupLat(), ride.getPickupLng(),
                            ride.getDropAddress(), ride.getDropLat(), ride.getDropLng(),
                            ride.getEstimatedFare(), d.getDistanceM() == null ? 0 : d.getDistanceM()));
        }
        if (!drivers.isEmpty()) {
            log.debug("Ride {}: offered to {} driver(s) within {} m", ride.getId(), drivers.size(), ride.getSearchRadiusM());
        }
        return drivers.size();
    }

    private void giveUp(Ride ride) {
        ride.setStatus(RideStatus.NO_DRIVER);
        offerService.withdrawOffers(ride.getId(), null);
        rideEvents.status(ride);
        log.info("Ride {}: no driver found, giving up", ride.getId());
    }
}

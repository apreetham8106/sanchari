package com.anvay.dispatch.service;

import com.anvay.dispatch.config.DispatchProperties;
import com.anvay.location.service.LocationService;
import com.anvay.common.util.GeoUtils;
import com.anvay.realtime.RealtimeNotifier;
import com.anvay.ride.domain.OfferStatus;
import com.anvay.ride.domain.Ride;
import com.anvay.ride.domain.RideOffer;
import com.anvay.ride.domain.RideStatus;
import com.anvay.ride.dto.RideOfferDto;
import com.anvay.ride.repository.RideOfferRepository;
import com.anvay.ride.repository.RideRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfferService {

    private final RideOfferRepository offers;
    private final RideRepository rides;
    private final LocationService location;
    private final RealtimeNotifier notifier;
    private final DispatchProperties props;

    public OfferService(RideOfferRepository offers, RideRepository rides, LocationService location,
                        RealtimeNotifier notifier, DispatchProperties props) {
        this.offers = offers;
        this.rides = rides;
        this.location = location;
        this.notifier = notifier;
        this.props = props;
    }

    /** Closes all still-open offers for a ride (taken, cancelled, or timed out) and tells those drivers. */
    @Transactional
    public void withdrawOffers(UUID rideId, UUID exceptDriverId) {
        for (RideOffer o : offers.findByRideIdAndStatus(rideId, OfferStatus.SENT)) {
            if (o.getDriverId().equals(exceptDriverId)) {
                continue;
            }
            o.setStatus(OfferStatus.EXPIRED);
            notifier.toUser(o.getDriverId(), "/queue/offer-withdrawn", Map.of("rideId", rideId));
        }
    }

    /** Offers a driver can still act on (used when the driver app reconnects or reloads). */
    @Transactional(readOnly = true)
    public List<RideOfferDto> listOpenOffers(UUID driverId) {
        Instant cutoff = Instant.now().minusSeconds(props.offerTtlSeconds() + 5L);
        var here = location.find(driverId).orElse(null);
        List<RideOfferDto> result = new ArrayList<>();
        for (RideOffer o : offers.findByDriverIdAndStatus(driverId, OfferStatus.SENT)) {
            if (o.getSentAt().isBefore(cutoff)) {
                continue;
            }
            Ride r = rides.findById(o.getRideId()).orElse(null);
            if (r == null || r.getStatus() != RideStatus.SEARCHING) {
                continue;
            }
            double distance = here == null ? 0
                    : GeoUtils.distanceMeters(here.getLat(), here.getLng(), r.getPickupLat(), r.getPickupLng());
            result.add(new RideOfferDto(r.getId(), o.getRadiusM(), o.getSentAt().plusSeconds(props.offerTtlSeconds()),
                    r.getPickupAddress(), r.getPickupLat(), r.getPickupLng(),
                    r.getDropAddress(), r.getDropLat(), r.getDropLng(), r.getEstimatedFare(), distance));
        }
        return result;
    }
}

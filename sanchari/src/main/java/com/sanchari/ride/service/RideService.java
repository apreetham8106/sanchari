package com.sanchari.ride.service;

import com.sanchari.common.dto.PageResponse;
import com.sanchari.common.enums.Role;
import com.sanchari.common.exception.BadRequestException;
import com.sanchari.common.exception.ConflictException;
import com.sanchari.common.exception.ForbiddenException;
import com.sanchari.common.exception.NotFoundException;
import com.sanchari.common.util.GeoUtils;
import com.sanchari.dispatch.config.DispatchProperties;
import com.sanchari.dispatch.service.OfferService;
import com.sanchari.driver.service.DriverAvailabilityService;
import com.sanchari.ride.domain.OfferStatus;
import com.sanchari.ride.domain.Ride;
import com.sanchari.ride.domain.RideOffer;
import com.sanchari.ride.domain.RideStatus;
import com.sanchari.ride.dto.BookRideRequest;
import com.sanchari.ride.dto.FareEstimateDto;
import com.sanchari.ride.dto.FareEstimateRequest;
import com.sanchari.ride.dto.RideDto;
import com.sanchari.ride.kafka.RideEventProducer;
import com.sanchari.ride.mapper.RideMapper;
import com.sanchari.ride.repository.RideOfferRepository;
import com.sanchari.ride.repository.RideRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RideService {

    private static final double MIN_TRIP_METERS = 200;
    private static final Set<RideStatus> DRIVER_ACTIONS = Set.of(RideStatus.ARRIVED, RideStatus.STARTED, RideStatus.COMPLETED);

    private final RideRepository rides;
    private final RideOfferRepository offers;
    private final FareService fares;
    private final RideEventProducer events;
    private final DispatchProperties dispatchProps;
    private final DriverAvailabilityService availability;
    private final OfferService offerService;
    private final RideMapper mapper;

    public RideService(RideRepository rides, RideOfferRepository offers, FareService fares, RideEventProducer events,
                       DispatchProperties dispatchProps, DriverAvailabilityService availability,
                       OfferService offerService, RideMapper mapper) {
        this.rides = rides;
        this.offers = offers;
        this.fares = fares;
        this.events = events;
        this.dispatchProps = dispatchProps;
        this.availability = availability;
        this.offerService = offerService;
        this.mapper = mapper;
    }

    // ---------------- rider ----------------

    public FareEstimateDto estimate(FareEstimateRequest req) {
        double meters = GeoUtils.distanceMeters(req.pickupLat(), req.pickupLng(), req.dropLat(), req.dropLng());
        return new FareEstimateDto(req.vehicleType(), round1(fares.roadKm(meters)),
                fares.estimate(req.vehicleType(), meters), "INR");
    }

    @Transactional
    public RideDto book(UUID riderId, BookRideRequest req) {
        if (rides.existsByRiderIdAndStatusIn(riderId, RideStatus.ACTIVE)) {
            throw new ConflictException("You already have an active ride");
        }
        double meters = GeoUtils.distanceMeters(req.pickupLat(), req.pickupLng(), req.dropLat(), req.dropLng());
        if (meters < MIN_TRIP_METERS) {
            throw new BadRequestException("Pickup and drop are too close");
        }
        Ride ride = new Ride();
        ride.setRiderId(riderId);
        ride.setVehicleType(req.vehicleType());
        ride.setPickupLat(req.pickupLat());
        ride.setPickupLng(req.pickupLng());
        ride.setPickupAddress(req.pickupAddress());
        ride.setDropLat(req.dropLat());
        ride.setDropLng(req.dropLng());
        ride.setDropAddress(req.dropAddress());
        ride.setStatus(RideStatus.SEARCHING);
        ride.setSearchRadiusM(dispatchProps.startRadiusM());
        ride.setEstimatedFare(fares.estimate(req.vehicleType(), meters));
        rides.save(ride);

        events.requested(ride);
        events.status(ride);
        return mapper.toDto(ride);
    }

    @Transactional
    public RideDto cancel(UUID rideId, UUID riderId) {
        Ride ride = lock(rideId);
        if (!ride.getRiderId().equals(riderId)) {
            throw new ForbiddenException("Not your ride");
        }
        RideStateMachine.assertTransition(ride.getStatus(), RideStatus.CANCELLED);
        ride.setStatus(RideStatus.CANCELLED);
        if (ride.getDriverId() != null) {
            availability.markFree(ride.getDriverId());
        }
        offerService.withdrawOffers(rideId, null);
        events.status(ride);
        return mapper.toDto(ride);
    }

    @Transactional(readOnly = true)
    public Optional<RideDto> activeForRider(UUID riderId) {
        return rides.findFirstByRiderIdAndStatusInOrderByCreatedAtDesc(riderId, RideStatus.ACTIVE).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public PageResponse<RideDto> riderHistory(UUID riderId, Pageable pageable) {
        return PageResponse.of(rides.findByRiderIdOrderByCreatedAtDesc(riderId, pageable).map(mapper::toDto));
    }

    // ---------------- driver ----------------

    @Transactional
    public RideDto accept(UUID rideId, UUID driverId) {
        Ride ride = lock(rideId);
        RideOffer offer = offers.findByRideIdAndDriverId(rideId, driverId)
                .orElseThrow(() -> new ForbiddenException("You were not offered this ride"));
        if (ride.getStatus() != RideStatus.SEARCHING) {
            throw new ConflictException("This ride is no longer available");
        }
        if (offer.getStatus() != OfferStatus.SENT) {
            throw new ConflictException("This offer has expired");
        }
        if (rides.existsByDriverIdAndStatusIn(driverId, RideStatus.DRIVER_BUSY)) {
            throw new ConflictException("Finish your current ride first");
        }
        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(Instant.now());
        offer.setStatus(OfferStatus.ACCEPTED);
        availability.markBusy(driverId);

        events.accepted(rideId, driverId);
        events.status(ride);
        return mapper.toDto(ride);
    }

    @Transactional
    public void reject(UUID rideId, UUID driverId) {
        RideOffer offer = offers.findByRideIdAndDriverId(rideId, driverId)
                .orElseThrow(() -> new ForbiddenException("You were not offered this ride"));
        if (offer.getStatus() == OfferStatus.SENT) {
            offer.setStatus(OfferStatus.REJECTED);
        }
    }

    @Transactional
    public RideDto driverTransition(UUID rideId, UUID driverId, RideStatus target) {
        if (!DRIVER_ACTIONS.contains(target)) {
            throw new BadRequestException("Not a driver action");
        }
        Ride ride = lock(rideId);
        if (!driverId.equals(ride.getDriverId())) {
            throw new ForbiddenException("Not your ride");
        }
        RideStateMachine.assertTransition(ride.getStatus(), target);
        ride.setStatus(target);
        if (target == RideStatus.COMPLETED) {
            ride.setFinalFare(ride.getEstimatedFare());
            ride.setCompletedAt(Instant.now());
            availability.markFree(driverId);
        }
        events.status(ride);
        return mapper.toDto(ride);
    }

    @Transactional(readOnly = true)
    public Optional<RideDto> activeForDriver(UUID driverId) {
        return rides.findFirstByDriverIdAndStatusInOrderByCreatedAtDesc(driverId, RideStatus.DRIVER_BUSY).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public PageResponse<RideDto> driverHistory(UUID driverId, Pageable pageable) {
        return PageResponse.of(rides.findByDriverIdOrderByCreatedAtDesc(driverId, pageable).map(mapper::toDto));
    }

    @Transactional(readOnly = true)
    public Optional<UUID> findActiveRideIdForDriver(UUID driverId) {
        return rides.findFirstByDriverIdAndStatusInOrderByCreatedAtDesc(driverId, RideStatus.DRIVER_BUSY).map(Ride::getId);
    }

    // ---------------- shared ----------------

    @Transactional(readOnly = true)
    public RideDto get(UUID rideId, UUID userId, Role role) {
        Ride ride = rides.findById(rideId).orElseThrow(() -> new NotFoundException("Ride not found"));
        if (!canView(ride, userId, role)) {
            throw new ForbiddenException("Not your ride");
        }
        return mapper.toDto(ride);
    }

    /** Used to authorise STOMP subscriptions to a ride's topic. */
    @Transactional(readOnly = true)
    public boolean isParticipant(UUID rideId, UUID userId, Role role) {
        return rides.findById(rideId).map(r -> canView(r, userId, role)).orElse(false);
    }

    @Transactional(readOnly = true)
    public PageResponse<RideDto> adminList(RideStatus status, Pageable pageable) {
        var page = status == null ? rides.findAll(pageable) : rides.findByStatus(status, pageable);
        return PageResponse.of(page.map(mapper::toDto));
    }

    private boolean canView(Ride ride, UUID userId, Role role) {
        return role == Role.ADMIN || userId.equals(ride.getRiderId()) || userId.equals(ride.getDriverId());
    }

    private Ride lock(UUID rideId) {
        return rides.findByIdForUpdate(rideId).orElseThrow(() -> new NotFoundException("Ride not found"));
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}

package com.sanchari.ride.kafka;

import com.sanchari.common.event.EventPublisher;
import com.sanchari.common.event.RideAcceptedEvent;
import com.sanchari.common.event.RideRequestedEvent;
import com.sanchari.common.event.RideStatusEvent;
import com.sanchari.common.event.Topics;
import com.sanchari.ride.domain.Ride;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RideEventProducer {

    private final EventPublisher events;

    public RideEventProducer(EventPublisher events) {
        this.events = events;
    }

    public void requested(Ride r) {
        events.publishAfterCommit(Topics.RIDE_REQUESTED, r.getId().toString(), "RIDE_REQUESTED",
                new RideRequestedEvent(r.getId(), r.getRiderId(), r.getVehicleType().name(), r.getPickupLat(), r.getPickupLng()));
    }

    public void accepted(UUID rideId, UUID driverId) {
        events.publishAfterCommit(Topics.RIDE_ACCEPTED, rideId.toString(), "RIDE_ACCEPTED",
                new RideAcceptedEvent(rideId, driverId));
    }

    public void status(Ride r) {
        events.publishAfterCommit(Topics.RIDE_STATUS, r.getId().toString(), "RIDE_STATUS",
                new RideStatusEvent(r.getId(), r.getRiderId(), r.getDriverId(), r.getStatus().name(), Instant.now()));
    }
}

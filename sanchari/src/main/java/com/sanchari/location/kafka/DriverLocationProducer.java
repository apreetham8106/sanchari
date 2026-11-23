package com.sanchari.location.kafka;

import com.sanchari.common.event.DriverLocationEvent;
import com.sanchari.common.event.EventPublisher;
import com.sanchari.common.event.Topics;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DriverLocationProducer {

    private final EventPublisher events;

    public DriverLocationProducer(EventPublisher events) {
        this.events = events;
    }

    /** Keyed by driverId so one driver's pings stay ordered within a partition. */
    public void publish(UUID driverId, double lat, double lng, Float heading) {
        events.publish(Topics.DRIVER_LOCATION, driverId.toString(), "DRIVER_LOCATION",
                new DriverLocationEvent(driverId, lat, lng, heading, Instant.now()));
    }
}

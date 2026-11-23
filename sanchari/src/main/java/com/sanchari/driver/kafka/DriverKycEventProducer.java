package com.sanchari.driver.kafka;

import com.sanchari.common.event.DriverKycEvent;
import com.sanchari.common.event.EventPublisher;
import com.sanchari.common.event.Topics;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DriverKycEventProducer {

    private final EventPublisher events;

    public DriverKycEventProducer(EventPublisher events) {
        this.events = events;
    }

    public void publish(UUID driverId, String subject, String status, String refId) {
        events.publishAfterCommit(Topics.DRIVER_KYC, driverId.toString(), "DRIVER_KYC",
                new DriverKycEvent(driverId, subject, status, refId));
    }
}

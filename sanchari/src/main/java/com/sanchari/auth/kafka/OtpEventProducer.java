package com.sanchari.auth.kafka;

import com.sanchari.auth.domain.OtpPurpose;
import com.sanchari.common.event.EventPublisher;
import com.sanchari.common.event.OtpSendEvent;
import com.sanchari.common.event.Topics;
import org.springframework.stereotype.Component;

@Component
public class OtpEventProducer {

    private final EventPublisher events;

    public OtpEventProducer(EventPublisher events) {
        this.events = events;
    }

    /** The code travels in the event only so notification-service can SMS it; the DB stores just a hash. */
    public void publish(String phone, String code, OtpPurpose purpose) {
        events.publishAfterCommit(Topics.OTP_SEND, phone, "OTP_SEND", new OtpSendEvent(phone, code, purpose.name()));
    }
}

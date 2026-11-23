package com.anvay.auth.kafka;

import com.anvay.auth.domain.OtpPurpose;
import com.anvay.common.event.EventPublisher;
import com.anvay.common.event.OtpSendEvent;
import com.anvay.common.event.Topics;
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

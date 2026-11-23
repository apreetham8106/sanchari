package com.anvay.notification.kafka;

import com.anvay.common.event.EventCodec;
import com.anvay.common.event.OtpSendEvent;
import com.anvay.common.event.Topics;
import com.anvay.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OtpSendConsumer {

    private final EventCodec codec;
    private final NotificationService notifications;

    public OtpSendConsumer(EventCodec codec, NotificationService notifications) {
        this.codec = codec;
        this.notifications = notifications;
    }

    @KafkaListener(topics = Topics.OTP_SEND, groupId = "anvay-notification")
    public void onOtp(String json) {
        OtpSendEvent e = codec.payload(json, OtpSendEvent.class);
        notifications.sendOtp(e.phone(), e.code());
    }
}

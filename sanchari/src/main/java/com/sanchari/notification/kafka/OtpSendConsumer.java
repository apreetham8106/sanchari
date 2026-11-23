package com.sanchari.notification.kafka;

import com.sanchari.common.event.EventCodec;
import com.sanchari.common.event.OtpSendEvent;
import com.sanchari.common.event.Topics;
import com.sanchari.notification.service.NotificationService;
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

    @KafkaListener(topics = Topics.OTP_SEND, groupId = "sanchari-notification")
    public void onOtp(String json) {
        OtpSendEvent e = codec.payload(json, OtpSendEvent.class);
        notifications.sendOtp(e.phone(), e.code());
    }
}

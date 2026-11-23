package com.sanchari.realtime;

import com.sanchari.common.event.DriverKycEvent;
import com.sanchari.common.event.EventCodec;
import com.sanchari.common.event.Topics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** driver.kyc -> the driver's own /user/queue/kyc and the admins' /topic/admin/kyc. */
@Component
public class KycStatusBroadcaster {

    private final EventCodec codec;
    private final RealtimeNotifier notifier;

    public KycStatusBroadcaster(EventCodec codec, RealtimeNotifier notifier) {
        this.codec = codec;
        this.notifier = notifier;
    }

    @KafkaListener(topics = Topics.DRIVER_KYC, groupId = "sanchari-realtime-kyc")
    public void onKyc(String json) {
        DriverKycEvent e = codec.payload(json, DriverKycEvent.class);
        notifier.toUser(e.driverId(), "/queue/kyc", e);
        notifier.toTopic("/topic/admin/kyc", e);
    }
}

package com.anvay.realtime;

import com.anvay.common.event.EventCodec;
import com.anvay.common.event.RideStatusEvent;
import com.anvay.common.event.Topics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** ride.status -> /topic/rides/{rideId} for the rider (and driver) following that ride. */
@Component
public class RideStatusBroadcaster {

    private final EventCodec codec;
    private final RealtimeNotifier notifier;

    public RideStatusBroadcaster(EventCodec codec, RealtimeNotifier notifier) {
        this.codec = codec;
        this.notifier = notifier;
    }

    @KafkaListener(topics = Topics.RIDE_STATUS, groupId = "anvay-realtime-status")
    public void onStatus(String json) {
        RideStatusEvent e = codec.payload(json, RideStatusEvent.class);
        notifier.toTopic("/topic/rides/" + e.rideId(), e);
    }
}

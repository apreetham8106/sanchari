package com.anvay.realtime;

import com.anvay.common.event.EventCodec;
import com.anvay.common.event.RideOfferEvent;
import com.anvay.common.event.Topics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** ride.offer -> the offered driver's private queue (/user/queue/offers). */
@Component
public class RideOfferBroadcaster {

    private final EventCodec codec;
    private final RealtimeNotifier notifier;

    public RideOfferBroadcaster(EventCodec codec, RealtimeNotifier notifier) {
        this.codec = codec;
        this.notifier = notifier;
    }

    @KafkaListener(topics = Topics.RIDE_OFFER, groupId = "anvay-realtime-offers")
    public void onOffer(String json) {
        RideOfferEvent e = codec.payload(json, RideOfferEvent.class);
        notifier.toUser(e.driverId(), "/queue/offers", e);
    }
}

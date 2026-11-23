package com.sanchari.dispatch.kafka;

import com.sanchari.common.event.EventCodec;
import com.sanchari.common.event.RideAcceptedEvent;
import com.sanchari.common.event.Topics;
import com.sanchari.dispatch.service.OfferService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Once a driver accepts, tell every other driver who was offered the ride that it is gone. */
@Component
public class RideAcceptedConsumer {

    private final EventCodec codec;
    private final OfferService offerService;

    public RideAcceptedConsumer(EventCodec codec, OfferService offerService) {
        this.codec = codec;
        this.offerService = offerService;
    }

    @KafkaListener(topics = Topics.RIDE_ACCEPTED, groupId = "sanchari-dispatch")
    public void onAccepted(String json) {
        RideAcceptedEvent e = codec.payload(json, RideAcceptedEvent.class);
        offerService.withdrawOffers(e.rideId(), e.driverId());
    }
}

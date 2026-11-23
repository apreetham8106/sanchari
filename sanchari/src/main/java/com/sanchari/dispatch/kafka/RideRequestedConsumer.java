package com.sanchari.dispatch.kafka;

import com.sanchari.common.event.EventCodec;
import com.sanchari.common.event.RideRequestedEvent;
import com.sanchari.common.event.Topics;
import com.sanchari.dispatch.service.DispatchService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RideRequestedConsumer {

    private final EventCodec codec;
    private final DispatchService dispatch;

    public RideRequestedConsumer(EventCodec codec, DispatchService dispatch) {
        this.codec = codec;
        this.dispatch = dispatch;
    }

    @KafkaListener(topics = Topics.RIDE_REQUESTED, groupId = "sanchari-dispatch")
    public void onRideRequested(String json) {
        RideRequestedEvent e = codec.payload(json, RideRequestedEvent.class);
        dispatch.process(e.rideId());
    }
}

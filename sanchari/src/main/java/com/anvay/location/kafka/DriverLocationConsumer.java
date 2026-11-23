package com.anvay.location.kafka;

import com.anvay.common.event.DriverLocationEvent;
import com.anvay.common.event.EventCodec;
import com.anvay.common.event.Topics;
import com.anvay.location.dto.DriverLocationMessage;
import com.anvay.location.repository.DriverLocationRepository;
import com.anvay.realtime.RealtimeNotifier;
import com.anvay.ride.service.RideService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DriverLocationConsumer {

    private final EventCodec codec;
    private final DriverLocationRepository repo;
    private final RideService rideService;
    private final RealtimeNotifier notifier;

    public DriverLocationConsumer(EventCodec codec, DriverLocationRepository repo, RideService rideService,
                                  RealtimeNotifier notifier) {
        this.codec = codec;
        this.repo = repo;
        this.rideService = rideService;
        this.notifier = notifier;
    }

    @KafkaListener(topics = Topics.DRIVER_LOCATION, groupId = "anvay-location")
    public void onLocation(String json) {
        DriverLocationEvent e = codec.payload(json, DriverLocationEvent.class);
        repo.upsert(e.driverId(), e.lat(), e.lng(), e.heading());
        // While the driver is on a ride, stream the position to the rider.
        rideService.findActiveRideIdForDriver(e.driverId()).ifPresent(rideId ->
                notifier.toTopic("/topic/rides/" + rideId + "/driver-location",
                        new DriverLocationMessage(rideId, e.driverId(), e.lat(), e.lng(), e.heading(), e.ts())));
    }
}

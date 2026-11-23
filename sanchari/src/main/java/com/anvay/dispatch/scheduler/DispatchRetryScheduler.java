package com.anvay.dispatch.scheduler;

import com.anvay.dispatch.service.DispatchService;
import com.anvay.ride.domain.RideStatus;
import com.anvay.ride.repository.RideRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Re-checks every SEARCHING ride: expires stale offers, widens the radius, and times out searches. */
@Component
public class DispatchRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(DispatchRetryScheduler.class);

    private final RideRepository rides;
    private final DispatchService dispatch;

    public DispatchRetryScheduler(RideRepository rides, DispatchService dispatch) {
        this.rides = rides;
        this.dispatch = dispatch;
    }

    @Scheduled(fixedDelayString = "${anvay.dispatch.tick-ms:3000}")
    public void tick() {
        for (UUID rideId : rides.findIdsByStatus(RideStatus.SEARCHING)) {
            try {
                dispatch.process(rideId);
            } catch (Exception e) {
                log.warn("Dispatch tick failed for ride {}: {}", rideId, e.getMessage());
            }
        }
    }
}

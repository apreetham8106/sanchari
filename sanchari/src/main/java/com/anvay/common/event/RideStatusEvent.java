package com.anvay.common.event;

import java.time.Instant;
import java.util.UUID;

public record RideStatusEvent(UUID rideId, UUID riderId, UUID driverId, String status, Instant at) {
}

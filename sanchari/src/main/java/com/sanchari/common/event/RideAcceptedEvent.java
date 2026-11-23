package com.sanchari.common.event;

import java.util.UUID;

public record RideAcceptedEvent(UUID rideId, UUID driverId) {
}

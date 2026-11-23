package com.anvay.common.event;

import java.util.UUID;

public record RideAcceptedEvent(UUID rideId, UUID driverId) {
}

package com.anvay.common.event;

import java.util.UUID;

public record RideRequestedEvent(UUID rideId, UUID riderId, String vehicleType, double pickupLat, double pickupLng) {
}

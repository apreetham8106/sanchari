package com.anvay.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RideOfferEvent(UUID rideId, UUID driverId, int radiusM, Instant expiresAt,
                             String pickupAddress, double pickupLat, double pickupLng,
                             String dropAddress, double dropLat, double dropLng,
                             BigDecimal estimatedFare, double distanceToPickupM) {
}

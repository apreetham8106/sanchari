package com.sanchari.ride.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RideOfferDto(UUID rideId, int radiusM, Instant expiresAt,
                           String pickupAddress, double pickupLat, double pickupLng,
                           String dropAddress, double dropLat, double dropLng,
                           BigDecimal estimatedFare, double distanceToPickupM) {
}

package com.sanchari.ride.dto;

import com.sanchari.common.enums.VehicleType;
import com.sanchari.ride.domain.RideStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RideDto(UUID id, UUID riderId, RideStatus status, VehicleType vehicleType,
                      double pickupLat, double pickupLng, String pickupAddress,
                      double dropLat, double dropLng, String dropAddress,
                      BigDecimal estimatedFare, BigDecimal finalFare, int searchRadiusM,
                      DriverSummaryDto driver, Instant createdAt, Instant acceptedAt, Instant completedAt) {
}

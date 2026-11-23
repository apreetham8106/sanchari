package com.sanchari.ride.dto;

import com.sanchari.common.enums.VehicleType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookRideRequest(@NotNull VehicleType vehicleType,
                              @NotNull @DecimalMin("-90") @DecimalMax("90") Double pickupLat,
                              @NotNull @DecimalMin("-180") @DecimalMax("180") Double pickupLng,
                              @Size(max = 255) String pickupAddress,
                              @NotNull @DecimalMin("-90") @DecimalMax("90") Double dropLat,
                              @NotNull @DecimalMin("-180") @DecimalMax("180") Double dropLng,
                              @Size(max = 255) String dropAddress) {
}

package com.sanchari.ride.dto;

import com.sanchari.common.enums.VehicleType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record FareEstimateRequest(@NotNull VehicleType vehicleType,
                                  @NotNull @DecimalMin("-90") @DecimalMax("90") Double pickupLat,
                                  @NotNull @DecimalMin("-180") @DecimalMax("180") Double pickupLng,
                                  @NotNull @DecimalMin("-90") @DecimalMax("90") Double dropLat,
                                  @NotNull @DecimalMin("-180") @DecimalMax("180") Double dropLng) {
}

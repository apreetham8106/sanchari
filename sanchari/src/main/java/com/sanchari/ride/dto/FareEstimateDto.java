package com.sanchari.ride.dto;

import com.sanchari.common.enums.VehicleType;
import java.math.BigDecimal;

public record FareEstimateDto(VehicleType vehicleType, double distanceKm, BigDecimal estimatedFare, String currency) {
}

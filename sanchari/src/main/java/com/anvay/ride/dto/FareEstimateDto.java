package com.anvay.ride.dto;

import com.anvay.common.enums.VehicleType;
import java.math.BigDecimal;

public record FareEstimateDto(VehicleType vehicleType, double distanceKm, BigDecimal estimatedFare, String currency) {
}

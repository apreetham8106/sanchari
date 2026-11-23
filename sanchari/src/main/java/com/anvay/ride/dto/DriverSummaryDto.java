package com.anvay.ride.dto;

import com.anvay.common.enums.VehicleType;
import java.math.BigDecimal;
import java.util.UUID;

public record DriverSummaryDto(UUID id, String name, String phone, BigDecimal rating,
                               String vehicleRegistration, VehicleType vehicleType, String vehicleModel) {
}

package com.sanchari.driver.dto;

import com.sanchari.common.enums.VehicleType;
import com.sanchari.driver.domain.RcStatus;
import java.util.UUID;

public record VehicleDto(UUID id, String registrationNo, VehicleType vehicleType, String makeModel, RcStatus rcStatus) {
}

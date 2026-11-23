package com.anvay.driver.dto;

import com.anvay.common.enums.VehicleType;
import com.anvay.driver.domain.RcStatus;
import java.util.UUID;

public record VehicleDto(UUID id, String registrationNo, VehicleType vehicleType, String makeModel, RcStatus rcStatus) {
}

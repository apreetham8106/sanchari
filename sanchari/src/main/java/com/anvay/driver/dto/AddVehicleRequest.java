package com.anvay.driver.dto;

import com.anvay.common.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddVehicleRequest(@NotBlank @Size(max = 20) String registrationNo,
                                @NotNull VehicleType vehicleType,
                                @Size(max = 80) String makeModel) {
}

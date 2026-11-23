package com.sanchari.admin.dto;

import com.sanchari.driver.domain.KycStatus;
import com.sanchari.driver.dto.VehicleDto;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AdminDriverDto(UUID userId, String fullName, String phone, KycStatus kycStatus, String licenseNumber,
                             LocalDate licenseExpiry, boolean online, List<VehicleDto> vehicles) {
}

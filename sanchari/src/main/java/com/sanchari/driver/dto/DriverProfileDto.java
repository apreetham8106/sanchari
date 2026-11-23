package com.sanchari.driver.dto;

import com.sanchari.driver.domain.KycStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DriverProfileDto(UUID userId, KycStatus kycStatus, String licenseNumber, LocalDate licenseExpiry,
                               boolean online, boolean available, BigDecimal rating, List<VehicleDto> vehicles) {
}

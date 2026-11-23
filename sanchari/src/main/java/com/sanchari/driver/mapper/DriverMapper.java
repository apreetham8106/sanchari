package com.sanchari.driver.mapper;

import com.sanchari.driver.domain.DriverProfile;
import com.sanchari.driver.domain.Vehicle;
import com.sanchari.driver.dto.DriverProfileDto;
import com.sanchari.driver.dto.VehicleDto;
import java.util.List;

public final class DriverMapper {
    private DriverMapper() {
    }

    public static VehicleDto toDto(Vehicle v) {
        return new VehicleDto(v.getId(), v.getRegistrationNo(), v.getVehicleType(), v.getMakeModel(), v.getRcStatus());
    }

    public static DriverProfileDto toDto(DriverProfile p, List<Vehicle> vehicles) {
        return new DriverProfileDto(p.getUserId(), p.getKycStatus(), p.getLicenseNumber(), p.getLicenseExpiry(),
                p.isOnline(), p.isAvailable(), p.getRating(), vehicles.stream().map(DriverMapper::toDto).toList());
    }
}

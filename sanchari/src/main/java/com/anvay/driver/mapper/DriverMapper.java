package com.anvay.driver.mapper;

import com.anvay.driver.domain.DriverProfile;
import com.anvay.driver.domain.Vehicle;
import com.anvay.driver.dto.DriverProfileDto;
import com.anvay.driver.dto.VehicleDto;
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

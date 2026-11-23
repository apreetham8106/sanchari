package com.anvay.ride.mapper;

import com.anvay.driver.domain.DriverProfile;
import com.anvay.driver.domain.RcStatus;
import com.anvay.driver.domain.Vehicle;
import com.anvay.driver.repository.DriverProfileRepository;
import com.anvay.driver.repository.VehicleRepository;
import com.anvay.ride.domain.Ride;
import com.anvay.ride.dto.DriverSummaryDto;
import com.anvay.ride.dto.RideDto;
import com.anvay.user.domain.User;
import com.anvay.user.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RideMapper {

    private final UserRepository users;
    private final DriverProfileRepository profiles;
    private final VehicleRepository vehicles;

    public RideMapper(UserRepository users, DriverProfileRepository profiles, VehicleRepository vehicles) {
        this.users = users;
        this.profiles = profiles;
        this.vehicles = vehicles;
    }

    public RideDto toDto(Ride r) {
        DriverSummaryDto driver = null;
        if (r.getDriverId() != null) {
            driver = driverSummary(r);
        }
        return new RideDto(r.getId(), r.getRiderId(), r.getStatus(), r.getVehicleType(),
                r.getPickupLat(), r.getPickupLng(), r.getPickupAddress(),
                r.getDropLat(), r.getDropLng(), r.getDropAddress(),
                r.getEstimatedFare(), r.getFinalFare(), r.getSearchRadiusM(),
                driver, r.getCreatedAt(), r.getAcceptedAt(), r.getCompletedAt());
    }

    private DriverSummaryDto driverSummary(Ride r) {
        User u = users.findById(r.getDriverId()).orElse(null);
        DriverProfile p = profiles.findById(r.getDriverId()).orElse(null);
        List<Vehicle> list = vehicles.findByDriverId(r.getDriverId());
        Vehicle v = list.stream()
                .filter(x -> x.getRcStatus() == RcStatus.VERIFIED && x.getVehicleType() == r.getVehicleType())
                .min(Comparator.comparing(Vehicle::getCreatedAt))
                .orElse(list.isEmpty() ? null : list.get(0));
        return new DriverSummaryDto(r.getDriverId(),
                u == null ? null : u.getFullName(),
                u == null ? null : u.getPhone(),
                p == null ? null : p.getRating(),
                v == null ? null : v.getRegistrationNo(),
                v == null ? null : v.getVehicleType(),
                v == null ? null : v.getMakeModel());
    }
}

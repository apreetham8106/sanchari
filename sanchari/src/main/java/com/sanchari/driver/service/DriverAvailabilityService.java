package com.sanchari.driver.service;

import com.sanchari.common.exception.BadRequestException;
import com.sanchari.common.exception.ConflictException;
import com.sanchari.common.exception.ForbiddenException;
import com.sanchari.common.exception.NotFoundException;
import com.sanchari.driver.domain.DriverProfile;
import com.sanchari.driver.domain.KycStatus;
import com.sanchari.driver.domain.RcStatus;
import com.sanchari.driver.dto.DriverProfileDto;
import com.sanchari.driver.mapper.DriverMapper;
import com.sanchari.driver.repository.DriverProfileRepository;
import com.sanchari.driver.repository.VehicleRepository;
import com.sanchari.location.dto.LocationUpdateRequest;
import com.sanchari.location.service.LocationService;
import com.sanchari.ride.domain.RideStatus;
import com.sanchari.ride.repository.RideRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverAvailabilityService {

    private final DriverProfileRepository profiles;
    private final VehicleRepository vehicles;
    private final RideRepository rides;
    private final LocationService location;

    public DriverAvailabilityService(DriverProfileRepository profiles, VehicleRepository vehicles,
                                     RideRepository rides, LocationService location) {
        this.profiles = profiles;
        this.vehicles = vehicles;
        this.rides = rides;
        this.location = location;
    }

    @Transactional
    public DriverProfileDto setOnline(UUID driverId, boolean online, Double lat, Double lng) {
        DriverProfile p = profiles.findById(driverId).orElseThrow(() -> new NotFoundException("Driver profile not found"));
        if (online) {
            if (p.getKycStatus() != KycStatus.VERIFIED) {
                throw new ForbiddenException("Complete KYC verification before going online");
            }
            if (!vehicles.existsByDriverIdAndRcStatus(driverId, RcStatus.VERIFIED)) {
                throw new ForbiddenException("Add and verify a vehicle before going online");
            }
            if (lat == null || lng == null) {
                throw new BadRequestException("Current location is required to go online");
            }
            boolean busy = rides.existsByDriverIdAndStatusIn(driverId, RideStatus.DRIVER_BUSY);
            p.setOnline(true);
            p.setAvailable(!busy);
            location.report(driverId, new LocationUpdateRequest(lat, lng, null));
        } else {
            if (rides.existsByDriverIdAndStatusIn(driverId, RideStatus.DRIVER_BUSY)) {
                throw new ConflictException("Finish your current ride before going offline");
            }
            p.setOnline(false);
            p.setAvailable(false);
        }
        return DriverMapper.toDto(p, vehicles.findByDriverId(driverId));
    }

    @Transactional
    public void forceOffline(UUID driverId) {
        profiles.findById(driverId).ifPresent(p -> {
            p.setOnline(false);
            p.setAvailable(false);
        });
    }

    @Transactional
    public void markBusy(UUID driverId) {
        profiles.findById(driverId).ifPresent(p -> p.setAvailable(false));
    }

    @Transactional
    public void markFree(UUID driverId) {
        profiles.findById(driverId).ifPresent(p -> p.setAvailable(p.isOnline()));
    }
}

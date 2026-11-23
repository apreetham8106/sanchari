package com.sanchari.driver.service;

import com.sanchari.common.exception.ConflictException;
import com.sanchari.common.exception.NotFoundException;
import com.sanchari.driver.domain.DriverProfile;
import com.sanchari.driver.domain.Vehicle;
import com.sanchari.driver.dto.AddVehicleRequest;
import com.sanchari.driver.dto.DriverProfileDto;
import com.sanchari.driver.dto.StartKycRequest;
import com.sanchari.driver.dto.VehicleDto;
import com.sanchari.driver.mapper.DriverMapper;
import com.sanchari.driver.repository.DriverProfileRepository;
import com.sanchari.driver.repository.VehicleRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverService {

    private final DriverProfileRepository profiles;
    private final VehicleRepository vehicles;
    private final DriverKycService kycService;

    public DriverService(DriverProfileRepository profiles, VehicleRepository vehicles, DriverKycService kycService) {
        this.profiles = profiles;
        this.vehicles = vehicles;
        this.kycService = kycService;
    }

    @Transactional
    public void createProfile(UUID userId) {
        if (!profiles.existsById(userId)) {
            profiles.save(new DriverProfile(userId));
        }
    }

    @Transactional(readOnly = true)
    public DriverProfileDto getProfile(UUID driverId) {
        DriverProfile p = profiles.findById(driverId).orElseThrow(() -> new NotFoundException("Driver profile not found"));
        return DriverMapper.toDto(p, vehicles.findByDriverId(driverId));
    }

    @Transactional
    public DriverProfileDto startKyc(UUID driverId, StartKycRequest req) {
        DriverProfile p = kycService.startDriverKyc(driverId, req);
        return DriverMapper.toDto(p, vehicles.findByDriverId(driverId));
    }

    @Transactional
    public VehicleDto addVehicle(UUID driverId, AddVehicleRequest req) {
        String reg = req.registrationNo().replaceAll("[\\s-]", "").toUpperCase();
        if (vehicles.existsByRegistrationNo(reg)) {
            throw new ConflictException("This vehicle is already registered");
        }
        Vehicle v = new Vehicle();
        v.setDriverId(driverId);
        v.setRegistrationNo(reg);
        v.setVehicleType(req.vehicleType());
        v.setMakeModel(req.makeModel());
        vehicles.save(v);
        kycService.startVehicleKyc(v);
        return DriverMapper.toDto(v);
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> listVehicles(UUID driverId) {
        return vehicles.findByDriverId(driverId).stream().map(DriverMapper::toDto).toList();
    }
}

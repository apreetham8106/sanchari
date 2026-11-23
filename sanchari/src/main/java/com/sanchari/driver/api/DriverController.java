package com.sanchari.driver.api;

import com.sanchari.driver.dto.AddVehicleRequest;
import com.sanchari.driver.dto.DriverProfileDto;
import com.sanchari.driver.dto.GoOnlineRequest;
import com.sanchari.driver.dto.StartKycRequest;
import com.sanchari.driver.dto.VehicleDto;
import com.sanchari.driver.service.DriverAvailabilityService;
import com.sanchari.driver.service.DriverService;
import com.sanchari.security.SanchariUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    private final DriverService drivers;
    private final DriverAvailabilityService availability;

    public DriverController(DriverService drivers, DriverAvailabilityService availability) {
        this.drivers = drivers;
        this.availability = availability;
    }

    @GetMapping("/profile")
    public DriverProfileDto profile(@AuthenticationPrincipal SanchariUserPrincipal p) {
        return drivers.getProfile(p.userId());
    }

    @PostMapping("/kyc")
    public DriverProfileDto startKyc(@AuthenticationPrincipal SanchariUserPrincipal p, @Valid @RequestBody StartKycRequest req) {
        return drivers.startKyc(p.userId(), req);
    }

    @GetMapping("/vehicles")
    public List<VehicleDto> vehicles(@AuthenticationPrincipal SanchariUserPrincipal p) {
        return drivers.listVehicles(p.userId());
    }

    @PostMapping("/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleDto addVehicle(@AuthenticationPrincipal SanchariUserPrincipal p, @Valid @RequestBody AddVehicleRequest req) {
        return drivers.addVehicle(p.userId(), req);
    }

    @PostMapping("/online")
    public DriverProfileDto online(@AuthenticationPrincipal SanchariUserPrincipal p, @Valid @RequestBody GoOnlineRequest req) {
        return availability.setOnline(p.userId(), req.online(), req.lat(), req.lng());
    }
}

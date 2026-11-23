package com.sanchari.admin.service;

import com.sanchari.admin.dto.AdminDriverDto;
import com.sanchari.admin.dto.DashboardStatsDto;
import com.sanchari.auth.service.TokenService;
import com.sanchari.common.dto.PageResponse;
import com.sanchari.common.enums.Role;
import com.sanchari.common.exception.BadRequestException;
import com.sanchari.common.exception.NotFoundException;
import com.sanchari.driver.domain.DriverProfile;
import com.sanchari.driver.domain.KycStatus;
import com.sanchari.driver.mapper.DriverMapper;
import com.sanchari.driver.repository.DriverProfileRepository;
import com.sanchari.driver.repository.VehicleRepository;
import com.sanchari.driver.service.DriverAvailabilityService;
import com.sanchari.driver.service.DriverKycService;
import com.sanchari.ride.domain.RideStatus;
import com.sanchari.ride.repository.RideRepository;
import com.sanchari.user.domain.User;
import com.sanchari.user.domain.UserStatus;
import com.sanchari.user.dto.UserDto;
import com.sanchari.user.mapper.UserMapper;
import com.sanchari.user.repository.UserRepository;
import com.sanchari.user.service.UserService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final UserRepository users;
    private final UserService userService;
    private final DriverProfileRepository profiles;
    private final VehicleRepository vehicles;
    private final RideRepository rides;
    private final DriverKycService kycService;
    private final DriverAvailabilityService availability;
    private final TokenService tokenService;

    public AdminService(UserRepository users, UserService userService, DriverProfileRepository profiles,
                        VehicleRepository vehicles, RideRepository rides, DriverKycService kycService,
                        DriverAvailabilityService availability, TokenService tokenService) {
        this.users = users;
        this.userService = userService;
        this.profiles = profiles;
        this.vehicles = vehicles;
        this.rides = rides;
        this.kycService = kycService;
        this.availability = availability;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto dashboard() {
        var startOfDay = LocalDate.now(IST).atStartOfDay(IST).toInstant();
        return new DashboardStatsDto(
                users.countByRole(Role.RIDER),
                users.countByRole(Role.DRIVER),
                profiles.countByOnlineTrue(),
                profiles.countByKycStatus(KycStatus.IN_PROGRESS),
                rides.countByCreatedAtAfter(startOfDay),
                rides.countByStatusIn(RideStatus.ACTIVE),
                rides.countByStatus(RideStatus.COMPLETED),
                rides.countByStatus(RideStatus.CANCELLED));
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminDriverDto> drivers(KycStatus status, Pageable pageable) {
        Page<DriverProfile> page = status == null ? profiles.findAll(pageable) : profiles.findByKycStatus(status, pageable);
        return PageResponse.of(page.map(this::toAdminDriver));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDto> users(Role role, String q, Pageable pageable) {
        return PageResponse.of(userService.search(role, q, pageable).map(UserMapper::toDto));
    }

    @Transactional
    public UserDto setBlocked(UUID userId, boolean blocked) {
        User u = userService.getById(userId);
        if (u.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admins cannot be blocked");
        }
        userService.setStatus(userId, blocked ? UserStatus.BLOCKED : UserStatus.ACTIVE);
        if (blocked) {
            tokenService.revokeAll(userId);
            if (u.getRole() == Role.DRIVER) {
                availability.forceOffline(userId);
            }
        }
        return UserMapper.toDto(u);
    }

    @Transactional
    public void reviewDriver(UUID driverId, boolean approve, String remarks) {
        kycService.reviewDriver(driverId, approve, remarks);
    }

    @Transactional
    public void reviewVehicle(UUID vehicleId, boolean approve, String remarks) {
        if (!vehicles.existsById(vehicleId)) {
            throw new NotFoundException("Vehicle not found");
        }
        kycService.reviewVehicle(vehicleId, approve, remarks);
    }

    private AdminDriverDto toAdminDriver(DriverProfile p) {
        User u = users.findById(p.getUserId()).orElse(null);
        var vs = vehicles.findByDriverId(p.getUserId()).stream().map(DriverMapper::toDto).toList();
        return new AdminDriverDto(p.getUserId(), u == null ? null : u.getFullName(), u == null ? null : u.getPhone(),
                p.getKycStatus(), p.getLicenseNumber(), p.getLicenseExpiry(), p.isOnline(), vs);
    }
}

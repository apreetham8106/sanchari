package com.sanchari.auth.service;

import com.sanchari.auth.domain.OtpPurpose;
import com.sanchari.auth.dto.AuthResponse;
import com.sanchari.auth.dto.OtpRequestDto;
import com.sanchari.auth.dto.OtpVerifyRequest;
import com.sanchari.auth.dto.PasswordLoginRequest;
import com.sanchari.auth.dto.RefreshTokenRequest;
import com.sanchari.auth.dto.RegisterRequest;
import com.sanchari.auth.dto.ResetPasswordRequest;
import com.sanchari.auth.dto.TokenPair;
import com.sanchari.common.dto.MessageResponse;
import com.sanchari.common.enums.Role;
import com.sanchari.common.exception.SanchariException;
import com.sanchari.common.exception.BadRequestException;
import com.sanchari.common.exception.ConflictException;
import com.sanchari.common.exception.ForbiddenException;
import com.sanchari.common.exception.NotFoundException;
import com.sanchari.common.exception.UnauthorizedException;
import com.sanchari.common.util.PhoneUtils;
import com.sanchari.driver.service.DriverService;
import com.sanchari.user.domain.User;
import com.sanchari.user.domain.UserStatus;
import com.sanchari.user.mapper.UserMapper;
import com.sanchari.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final OtpService otpService;
    private final TokenService tokenService;
    private final DriverService driverService;

    public AuthService(UserRepository users, PasswordEncoder encoder, OtpService otpService,
                       TokenService tokenService, DriverService driverService) {
        this.users = users;
        this.encoder = encoder;
        this.otpService = otpService;
        this.tokenService = tokenService;
        this.driverService = driverService;
    }

    @Transactional
    public MessageResponse register(RegisterRequest req) {
        if (req.role() == Role.ADMIN) {
            throw new BadRequestException("Role not allowed");
        }
        String phone = PhoneUtils.normalize(req.phone());
        if (users.existsByPhone(phone)) {
            throw new ConflictException("An account with this phone already exists");
        }
        String email = req.email() == null || req.email().isBlank() ? null : req.email().trim().toLowerCase();
        if (email != null && users.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }

        User u = new User();
        u.setPhone(phone);
        u.setEmail(email);
        u.setFullName(req.fullName().trim());
        u.setRole(req.role());
        u.setStatus(UserStatus.ACTIVE);
        u.setPhoneVerified(false);
        if (req.password() != null && !req.password().isBlank()) {
            u.setPasswordHash(encoder.encode(req.password()));
        }
        users.save(u);

        if (u.getRole() == Role.DRIVER) {
            driverService.createProfile(u.getId());
        }
        otpService.send(phone, OtpPurpose.SIGNUP);
        return new MessageResponse("OTP sent to " + phone);
    }

    @Transactional
    public MessageResponse requestOtp(OtpRequestDto req) {
        String phone = PhoneUtils.normalize(req.phone());
        User u = users.findByPhone(phone).orElseThrow(() -> new NotFoundException("No account for this phone. Please register."));
        if (u.getStatus() == UserStatus.BLOCKED) {
            throw new ForbiddenException("Account is blocked");
        }
        otpService.send(phone, req.purpose());
        return new MessageResponse("OTP sent to " + phone);
    }

    @Transactional(noRollbackFor = SanchariException.class)
    public AuthResponse verifyOtp(OtpVerifyRequest req) {
        if (req.purpose() == OtpPurpose.RESET_PASSWORD) {
            throw new BadRequestException("Use the password reset endpoint");
        }
        String phone = PhoneUtils.normalize(req.phone());
        otpService.verify(phone, req.code(), req.purpose());
        User u = users.findByPhone(phone).orElseThrow(() -> new UnauthorizedException("Invalid or expired OTP"));
        ensureCanLogin(u, false);
        u.setPhoneVerified(true);
        return toResponse(u);
    }

    @Transactional
    public AuthResponse loginWithPassword(PasswordLoginRequest req) {
        String phone = PhoneUtils.normalize(req.phone());
        User u = users.findByPhone(phone).orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (u.getPasswordHash() == null || !encoder.matches(req.password(), u.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        ensureCanLogin(u, true);
        return toResponse(u);
    }

    @Transactional(noRollbackFor = SanchariException.class)
    public MessageResponse resetPassword(ResetPasswordRequest req) {
        String phone = PhoneUtils.normalize(req.phone());
        otpService.verify(phone, req.code(), OtpPurpose.RESET_PASSWORD);
        User u = users.findByPhone(phone).orElseThrow(() -> new UnauthorizedException("Invalid or expired OTP"));
        u.setPasswordHash(encoder.encode(req.newPassword()));
        u.setPhoneVerified(true);
        tokenService.revokeAll(u.getId());
        return new MessageResponse("Password updated");
    }

    @Transactional
    public TokenPair refresh(RefreshTokenRequest req) {
        return tokenService.refresh(req.refreshToken());
    }

    @Transactional
    public MessageResponse logout(RefreshTokenRequest req) {
        tokenService.revoke(req.refreshToken());
        return new MessageResponse("Logged out");
    }

    private void ensureCanLogin(User u, boolean requireVerifiedPhone) {
        if (u.getStatus() == UserStatus.BLOCKED) {
            throw new ForbiddenException("Account is blocked");
        }
        if (requireVerifiedPhone && !u.isPhoneVerified()) {
            throw new ForbiddenException("Phone not verified. Log in with OTP first.");
        }
    }

    private AuthResponse toResponse(User u) {
        TokenPair t = tokenService.issue(u);
        return new AuthResponse(t.accessToken(), t.refreshToken(), t.expiresInSec(), UserMapper.toDto(u));
    }
}

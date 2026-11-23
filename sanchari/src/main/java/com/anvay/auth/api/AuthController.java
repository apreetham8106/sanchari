package com.anvay.auth.api;

import com.anvay.auth.dto.AuthResponse;
import com.anvay.auth.dto.OtpRequestDto;
import com.anvay.auth.dto.OtpVerifyRequest;
import com.anvay.auth.dto.PasswordLoginRequest;
import com.anvay.auth.dto.RefreshTokenRequest;
import com.anvay.auth.dto.RegisterRequest;
import com.anvay.auth.dto.ResetPasswordRequest;
import com.anvay.auth.dto.TokenPair;
import com.anvay.auth.service.AuthService;
import com.anvay.common.dto.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse register(@Valid @RequestBody RegisterRequest req) {
        return auth.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody PasswordLoginRequest req) {
        return auth.loginWithPassword(req);
    }

    @PostMapping("/otp/request")
    public MessageResponse requestOtp(@Valid @RequestBody OtpRequestDto req) {
        return auth.requestOtp(req);
    }

    @PostMapping("/otp/verify")
    public AuthResponse verifyOtp(@Valid @RequestBody OtpVerifyRequest req) {
        return auth.verifyOtp(req);
    }

    @PostMapping("/password/reset")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        return auth.resetPassword(req);
    }

    @PostMapping("/refresh")
    public TokenPair refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return auth.refresh(req);
    }

    @PostMapping("/logout")
    public MessageResponse logout(@Valid @RequestBody RefreshTokenRequest req) {
        return auth.logout(req);
    }
}

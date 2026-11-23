package com.sanchari.auth.dto;

import com.sanchari.auth.domain.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtpVerifyRequest(@NotBlank String phone, @NotBlank String code, @NotNull OtpPurpose purpose) {
}

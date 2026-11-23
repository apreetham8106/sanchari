package com.sanchari.auth.dto;

import com.sanchari.auth.domain.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtpRequestDto(@NotBlank String phone, @NotNull OtpPurpose purpose) {
}

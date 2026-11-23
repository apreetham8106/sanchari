package com.anvay.auth.dto;

import com.anvay.auth.domain.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtpVerifyRequest(@NotBlank String phone, @NotBlank String code, @NotNull OtpPurpose purpose) {
}

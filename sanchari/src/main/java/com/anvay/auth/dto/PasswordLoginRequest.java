package com.anvay.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordLoginRequest(@NotBlank String phone, @NotBlank String password) {
}

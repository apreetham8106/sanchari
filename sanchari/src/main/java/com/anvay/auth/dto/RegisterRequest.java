package com.anvay.auth.dto;

import com.anvay.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank @Size(max = 120) String fullName,
                              @NotBlank String phone,
                              @Email @Size(max = 255) String email,
                              @NotNull Role role,
                              @Size(min = 8, max = 72) String password) {
}

package com.sanchari.user.dto;

import com.sanchari.common.enums.Role;
import com.sanchari.user.domain.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record UserDto(UUID id, String phone, String email, String fullName, Role role,
                      UserStatus status, boolean phoneVerified, Instant createdAt) {
}

package com.anvay.user.dto;

import com.anvay.common.enums.Role;
import com.anvay.user.domain.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record UserDto(UUID id, String phone, String email, String fullName, Role role,
                      UserStatus status, boolean phoneVerified, Instant createdAt) {
}

package com.sanchari.user.mapper;

import com.sanchari.user.domain.User;
import com.sanchari.user.dto.UserDto;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getPhone(), u.getEmail(), u.getFullName(), u.getRole(),
                u.getStatus(), u.isPhoneVerified(), u.getCreatedAt());
    }
}

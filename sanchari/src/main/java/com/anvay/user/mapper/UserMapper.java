package com.anvay.user.mapper;

import com.anvay.user.domain.User;
import com.anvay.user.dto.UserDto;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getPhone(), u.getEmail(), u.getFullName(), u.getRole(),
                u.getStatus(), u.isPhoneVerified(), u.getCreatedAt());
    }
}

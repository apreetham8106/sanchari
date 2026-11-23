package com.anvay.auth.dto;

import com.anvay.user.dto.UserDto;

public record AuthResponse(String accessToken, String refreshToken, long expiresInSec, UserDto user) {
}

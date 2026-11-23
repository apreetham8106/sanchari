package com.sanchari.auth.dto;

import com.sanchari.user.dto.UserDto;

public record AuthResponse(String accessToken, String refreshToken, long expiresInSec, UserDto user) {
}

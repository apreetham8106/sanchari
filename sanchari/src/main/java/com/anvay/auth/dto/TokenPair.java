package com.anvay.auth.dto;

public record TokenPair(String accessToken, String refreshToken, long expiresInSec) {
}

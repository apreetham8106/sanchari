package com.sanchari.auth.dto;

public record TokenPair(String accessToken, String refreshToken, long expiresInSec) {
}

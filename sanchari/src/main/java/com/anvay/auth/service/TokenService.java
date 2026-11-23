package com.anvay.auth.service;

import com.anvay.auth.domain.RefreshToken;
import com.anvay.auth.dto.TokenPair;
import com.anvay.auth.repository.RefreshTokenRepository;
import com.anvay.common.exception.ForbiddenException;
import com.anvay.common.exception.UnauthorizedException;
import com.anvay.security.JwtService;
import com.anvay.user.domain.User;
import com.anvay.user.domain.UserStatus;
import com.anvay.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final JwtService jwt;
    private final RefreshTokenRepository refreshTokens;
    private final UserRepository users;

    public TokenService(JwtService jwt, RefreshTokenRepository refreshTokens, UserRepository users) {
        this.jwt = jwt;
        this.refreshTokens = refreshTokens;
        this.users = users;
    }

    @Transactional
    public TokenPair issue(User user) {
        String access = jwt.generateAccessToken(user.getId(), user.getRole());
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String refresh = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setTokenHash(sha256(refresh));
        rt.setExpiresAt(Instant.now().plus(jwt.refreshTtl()));
        refreshTokens.save(rt);

        return new TokenPair(access, refresh, jwt.accessTtlSeconds());
    }

    /** Rotates: the presented refresh token is revoked and a new pair is returned. */
    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        RefreshToken rt = refreshTokens.findByTokenHashAndRevokedFalse(sha256(rawRefreshToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }
        User user = users.findById(rt.getUserId()).orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ForbiddenException("Account is blocked");
        }
        rt.setRevoked(true);
        return issue(user);
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        refreshTokens.findByTokenHashAndRevokedFalse(sha256(rawRefreshToken)).ifPresent(t -> t.setRevoked(true));
    }

    @Transactional
    public void revokeAll(UUID userId) {
        refreshTokens.revokeAllByUserId(userId);
    }

    private static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

package com.anvay.security;

import com.anvay.common.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;
        byte[] bytes = props.secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("anvay.jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccessToken(UUID userId, Role role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofMinutes(props.accessTtlMinutes()))))
                .signWith(key)
                .compact();
    }

    public long accessTtlSeconds() {
        return Duration.ofMinutes(props.accessTtlMinutes()).toSeconds();
    }

    public Duration refreshTtl() {
        return Duration.ofDays(props.refreshTtlDays());
    }

    /** Returns the principal if the token is valid and not expired. */
    public Optional<AnvayUserPrincipal> parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(new AnvayUserPrincipal(UUID.fromString(claims.getSubject()),
                    Role.valueOf(claims.get("role", String.class))));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}

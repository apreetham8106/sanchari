package com.sanchari.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sanchari.common.enums.Role;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-unit-test-secret";

    @Test
    void roundTrip() {
        JwtService jwt = new JwtService(new JwtProperties(SECRET, 15, 30));
        UUID id = UUID.randomUUID();
        var principal = jwt.parse(jwt.generateAccessToken(id, Role.DRIVER)).orElseThrow();
        assertEquals(id, principal.userId());
        assertEquals(Role.DRIVER, principal.role());
    }

    @Test
    void tamperedOrForeignTokenIsRejected() {
        JwtService jwt = new JwtService(new JwtProperties(SECRET, 15, 30));
        JwtService other = new JwtService(new JwtProperties("another-secret-another-secret-another-secret!!", 15, 30));
        String token = other.generateAccessToken(UUID.randomUUID(), Role.ADMIN);
        assertTrue(jwt.parse(token).isEmpty());
        assertTrue(jwt.parse("not-a-jwt").isEmpty());
    }

    @Test
    void shortSecretIsRefused() {
        assertThrows(IllegalStateException.class, () -> new JwtService(new JwtProperties("short", 15, 30)));
    }
}

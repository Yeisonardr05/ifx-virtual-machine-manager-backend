package com.ifx.vm_manager.infrastructure.adapters.output.security;

import com.ifx.vm_manager.domain.model.Role;
import com.ifx.vm_manager.domain.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secret", "test-jwt-secret-key-at-least-32-bytes-long-for-hs256");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86_400_000L);
    }

    @Test
    void generateToken_andExtractClaims() {
        User user = User.builder()
                .id(42L)
                .name("Ada")
                .email("ada@example.com")
                .password("x")
                .role(Role.ADMIN)
                .build();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractEmail(token)).isEqualTo("ada@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtService.extractName(token)).isEqualTo("Ada");
        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseForMalformedToken() {
        assertThat(jwtService.isTokenValid("not-a-jwt")).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(
                "test-jwt-secret-key-at-least-32-bytes-long-for-hs256".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("a@a.com")
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(key)
                .compact();

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }
}

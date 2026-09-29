package com.example.jwtnimbus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.jwtnimbus.entity.Role;
import com.example.jwtnimbus.entity.User;
import com.example.jwtnimbus.exception.JwtValidationException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final Instant NOW = Instant.parse("2026-09-29T02:00:00Z");

    @Test
    void generatesAndValidatesNimbusToken() {
        var service = new JwtService(SECRET, 3_600_000, "test-issuer", Clock.fixed(NOW, ZoneOffset.UTC));
        var user = user("student@example.com");

        var token = service.generateToken(user);

        assertThat(token.split("\\.")).hasSize(3);
        assertThat(service.extractUsername(token)).isEqualTo("student@example.com");
        assertThat(service.isTokenValid(token, user)).isTrue();
    }

    @Test
    void rejectsExpiredToken() {
        var mintingService = new JwtService(SECRET, 1_000, "test-issuer", Clock.fixed(NOW, ZoneOffset.UTC));
        var token = mintingService.generateToken(user("student@example.com"));
        var validatingService = new JwtService(
                SECRET,
                1_000,
                "test-issuer",
                Clock.fixed(NOW.plusSeconds(32), ZoneOffset.UTC)
        );

        assertThatThrownBy(() -> validatingService.extractUsername(token))
                .isInstanceOf(JwtValidationException.class)
                .hasMessage("JWT đã hết hạn");
    }

    @Test
    void rejectsTamperedToken() {
        var service = new JwtService(SECRET, 3_600_000, "test-issuer", Clock.fixed(NOW, ZoneOffset.UTC));
        var token = service.generateToken(user("student@example.com"));
        var tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        assertThatThrownBy(() -> service.extractUsername(tampered))
                .isInstanceOf(JwtValidationException.class);
    }

    private User user(String email) {
        var user = new User();
        user.setEmail(email);
        user.setPassword("encoded");
        user.setFullName("Student");
        user.setRole(Role.USER);
        return user;
    }
}

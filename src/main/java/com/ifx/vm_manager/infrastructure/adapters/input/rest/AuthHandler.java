package com.ifx.vm_manager.infrastructure.adapters.input.rest;

import com.ifx.vm_manager.application.dto.request.LoginRequest;
import com.ifx.vm_manager.application.dto.response.AuthResponse;
import com.ifx.vm_manager.domain.ports.input.AuthUseCase;
import com.ifx.vm_manager.infrastructure.adapters.output.security.JwtService;
import com.ifx.vm_manager.infrastructure.exceptions.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHandler {

    private static final String AUTH_COOKIE_NAME = "auth-token";

    private final AuthUseCase authUseCase;
    private final JwtService jwtService;
    private final Validator validator;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Strict}")
    private String cookieSameSite;

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .flatMap(this::validate)
                .flatMap(loginRequest -> authUseCase.authenticate(loginRequest.email(), loginRequest.password()))
                .flatMap(user -> {
                    String token = jwtService.generateToken(user);
                    AuthResponse authResponse = new AuthResponse(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getRole().name()
                    );
                    ResponseCookie cookie = buildAuthCookie(token);
                    log.info("User logged in: {}", user.getEmail());
                    return ServerResponse.ok()
                            .cookie(cookie)
                            .bodyValue(authResponse);
                });
    }

    public Mono<ServerResponse> logout(ServerRequest request) {
        ResponseCookie expiredCookie = ResponseCookie.from(AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        return ServerResponse.ok()
                .cookie(expiredCookie)
                .bodyValue("Logged out successfully");
    }

    private ResponseCookie buildAuthCookie(String token) {
        return ResponseCookie.from(AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(Duration.ofHours(24))
                .build();
    }

    private <T> Mono<T> validate(T body) {
        Set<ConstraintViolation<T>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            return Mono.error(new ValidationException(message));
        }
        return Mono.just(body);
    }
}

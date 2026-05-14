package com.ifx.vm_manager.infrastructure.adapters.input.rest;

import com.ifx.vm_manager.application.dto.response.AuthResponse;
import com.ifx.vm_manager.domain.model.Role;
import com.ifx.vm_manager.domain.model.User;
import com.ifx.vm_manager.domain.ports.input.AuthUseCase;
import com.ifx.vm_manager.infrastructure.adapters.output.security.JwtService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthHandlerTest {

    @Mock
    private AuthUseCase authUseCase;

    @Mock
    private JwtService jwtService;

    private Validator validator;
    private AuthHandler handler;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        handler = new AuthHandler(authUseCase, jwtService, validator);
        ReflectionTestUtils.setField(handler, "cookieSecure", false);
        ReflectionTestUtils.setField(handler, "cookieSameSite", "Strict");
        RouterFunction<ServerResponse> router = new AuthRouter().authRoutes(handler);
        client = WebTestClient.bindToRouterFunction(router).build();
    }

    @Test
    void login_setsCookieAndReturnsBody() {
        User user = User.builder()
                .id(1L)
                .name("Ada")
                .email("ada@example.com")
                .password("hash")
                .role(Role.ADMIN)
                .build();

        when(authUseCase.authenticate("ada@example.com", "secret")).thenReturn(Mono.just(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        client.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"email":"ada@example.com","password":"secret"}
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectCookie()
                .exists("auth-token")
                .expectBody(AuthResponse.class)
                .value(body -> {
                    assertThat(body.email()).isEqualTo("ada@example.com");
                    assertThat(body.role()).isEqualTo("ADMIN");
                });

        verify(jwtService).generateToken(user);
    }

    @Test
    void logout_clearsCookie() {
        client.post()
                .uri("/logout")
                .exchange()
                .expectStatus()
                .isOk()
                .expectCookie()
                .exists("auth-token");
    }

}
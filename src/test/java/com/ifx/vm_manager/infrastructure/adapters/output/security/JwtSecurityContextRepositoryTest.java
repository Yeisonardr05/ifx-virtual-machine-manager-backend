package com.ifx.vm_manager.infrastructure.adapters.output.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtSecurityContextRepositoryTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtSecurityContextRepository repository;

    @Test
    void save_returnsEmpty() {
        StepVerifier.create(repository.save(MockServerWebExchange.from(MockServerHttpRequest.get("/")), null))
                .verifyComplete();
    }

    @Test
    void load_returnsEmptyWhenNoCookie() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));

        StepVerifier.create(repository.load(exchange)).verifyComplete();
    }

    @Test
    void load_returnsEmptyWhenTokenInvalid() {
        MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
        cookies.add("auth-token", new HttpCookie("auth-token", "bad"));
        MockServerHttpRequest request = MockServerHttpRequest.get("/").cookies(cookies).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtService.isTokenValid("bad")).thenReturn(false);

        StepVerifier.create(repository.load(exchange)).verifyComplete();
    }

    @Test
    void load_buildsContextWhenTokenValid() {
        MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
        cookies.add("auth-token", new HttpCookie("auth-token", "good"));
        MockServerHttpRequest request = MockServerHttpRequest.get("/").cookies(cookies).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtService.isTokenValid("good")).thenReturn(true);
        when(jwtService.extractEmail("good")).thenReturn("user@example.com");
        when(jwtService.extractRole("good")).thenReturn("ADMIN");

        StepVerifier.create(repository.load(exchange))
                .assertNext(ctx -> assertThat(ctx.getAuthentication().getName()).isEqualTo("user@example.com"))
                .verifyComplete();
    }

    @Test
    void load_swallowsErrorsDuringParsing() {
        MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
        cookies.add("auth-token", new HttpCookie("auth-token", "throws"));
        MockServerHttpRequest request = MockServerHttpRequest.get("/").cookies(cookies).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtService.isTokenValid(anyString())).thenThrow(new IllegalStateException("parse"));

        StepVerifier.create(repository.load(exchange)).verifyComplete();
    }
}

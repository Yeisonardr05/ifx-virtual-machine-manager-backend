package com.ifx.vm_manager.infrastructure.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void mapsNotFound() {
        MockServerWebExchange exchange = exchange();
        GlobalExceptionHandler handler = new GlobalExceptionHandler(jsonMapper);

        StepVerifier.create(handler.handle(exchange, new NotFoundException("missing")))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void mapsUnauthorized() {
        MockServerWebExchange exchange = exchange();
        GlobalExceptionHandler handler = new GlobalExceptionHandler(jsonMapper);

        StepVerifier.create(handler.handle(exchange, new UnauthorizedException("nope")))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void mapsAuthenticationException() {
        MockServerWebExchange exchange = exchange();
        GlobalExceptionHandler handler = new GlobalExceptionHandler(jsonMapper);

        StepVerifier.create(handler.handle(exchange, new BadCredentialsException("bad")))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void mapsAccessDenied() {
        MockServerWebExchange exchange = exchange();
        GlobalExceptionHandler handler = new GlobalExceptionHandler(jsonMapper);

        StepVerifier.create(handler.handle(exchange, new AccessDeniedException("denied")))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void mapsValidationException() {
        MockServerWebExchange exchange = exchange();
        GlobalExceptionHandler handler = new GlobalExceptionHandler(jsonMapper);

        StepVerifier.create(handler.handle(exchange, new ValidationException("invalid")))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void mapsBusinessException() {
        MockServerWebExchange exchange = exchange();
        GlobalExceptionHandler handler = new GlobalExceptionHandler(jsonMapper);

        StepVerifier.create(handler.handle(exchange, new BusinessException("biz")))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void mapsResponseStatusException() {
        MockServerWebExchange exchange = exchange();
        GlobalExceptionHandler handler = new GlobalExceptionHandler(jsonMapper);

        StepVerifier.create(handler.handle(exchange, new ResponseStatusException(HttpStatus.BAD_GATEWAY, "gw")))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void mapsUnknownException() {
        MockServerWebExchange exchange = exchange();
        GlobalExceptionHandler handler = new GlobalExceptionHandler(jsonMapper);

        StepVerifier.create(handler.handle(exchange, new IllegalStateException("boom")))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void completesResponseWhenSerializationFails() throws Exception {
        tools.jackson.databind.ObjectMapper broken = mock(tools.jackson.databind.ObjectMapper.class);
        when(broken.writeValueAsBytes(any())).thenThrow(new RuntimeException("cannot serialize"));

        MockServerWebExchange exchange = exchange();
        GlobalExceptionHandler handler = new GlobalExceptionHandler(broken);

        StepVerifier.create(handler.handle(exchange, new NotFoundException("x"))).verifyComplete();
    }

    private static MockServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/vms/1"));
    }
}

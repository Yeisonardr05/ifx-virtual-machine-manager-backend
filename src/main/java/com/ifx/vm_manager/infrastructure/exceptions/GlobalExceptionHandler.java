package com.ifx.vm_manager.infrastructure.exceptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String errorCode;
        String message;

        if (ex instanceof NotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorCode = "NOT_FOUND";
            message = ex.getMessage();
        } else if (ex instanceof UnauthorizedException || ex instanceof AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
            errorCode = "UNAUTHORIZED";
            message = ex.getMessage();
        } else if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            errorCode = "FORBIDDEN";
            message = "Access denied";
        } else if (ex instanceof ValidationException) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
            errorCode = "VALIDATION_ERROR";
            message = ex.getMessage();
        } else if (ex instanceof BusinessException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = "BAD_REQUEST";
            message = ex.getMessage();
        } else if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            errorCode = status.getReasonPhrase().toUpperCase().replace(" ", "_");
            message = rse.getReason() != null ? rse.getReason() : rse.getMessage();
        } else {
            log.error("Unhandled exception on path {}: {}", exchange.getRequest().getPath(), ex.getMessage(), ex);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = "INTERNAL_SERVER_ERROR";
            message = "An unexpected error occurred";
        }

        return writeErrorResponse(exchange, status, errorCode, message);
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String errorCode, String message) {
        ErrorResponse errorResponse = ErrorResponse.of(
                status.value(),
                errorCode,
                message,
                exchange.getRequest().getPath().value()
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Failed to serialize error response", e);
            return exchange.getResponse().setComplete();
        }
    }
}

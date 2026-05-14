package com.ifx.vm_manager.infrastructure.adapters.input.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class VmWebSocketHandler implements WebSocketHandler {

    private final VmEventPublisher eventPublisher;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("WebSocket client connected: {}", session.getId());

        return session.send(
                eventPublisher.getEventFlux()
                        .map(session::textMessage)
                        .doOnError(e -> log.error("Error sending WebSocket message", e))
        ).doOnTerminate(() -> log.info("WebSocket client disconnected: {}", session.getId()));
    }
}

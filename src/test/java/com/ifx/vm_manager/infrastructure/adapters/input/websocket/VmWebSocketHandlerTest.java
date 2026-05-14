package com.ifx.vm_manager.infrastructure.adapters.input.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VmWebSocketHandlerTest {

    @Test
    void handle_sendsMessagesFromPublisher() {
        VmEventPublisher publisher = mock(VmEventPublisher.class);
        WebSocketSession session = mock(WebSocketSession.class);
        WebSocketMessage message = mock(WebSocketMessage.class);

        when(publisher.getEventFlux()).thenReturn(Flux.just("payload"));
        when(session.textMessage("payload")).thenReturn(message);
        when(session.send(any())).thenReturn(Mono.empty());

        VmWebSocketHandler handler = new VmWebSocketHandler(publisher);

        StepVerifier.create(handler.handle(session)).verifyComplete();

        verify(session).send(any());
    }
}

package com.ifx.vm_manager.infrastructure.config;

import com.ifx.vm_manager.infrastructure.adapters.input.websocket.VmWebSocketHandler;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WebSocketConfigTest {

    @Test
    void registersWebSocketMappingAndAdapter() {
        WebSocketConfig config = new WebSocketConfig();
        VmWebSocketHandler handler = mock(VmWebSocketHandler.class);

        HandlerMapping mapping = config.webSocketHandlerMapping(handler);
        assertThat(mapping).isInstanceOf(SimpleUrlHandlerMapping.class);

        WebSocketHandlerAdapter adapter = config.webSocketHandlerAdapter();
        assertThat(adapter).isNotNull();
    }
}

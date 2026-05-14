package com.ifx.vm_manager.infrastructure.adapters.input.websocket;

import com.ifx.vm_manager.application.dto.response.VmEventMessage;
import com.ifx.vm_manager.domain.model.VirtualMachine;
import com.ifx.vm_manager.domain.model.VmEventType;
import com.ifx.vm_manager.domain.ports.output.VmEventPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class VmEventPublisher implements VmEventPort {

    private final Sinks.Many<String> sink;
    private final ObjectMapper objectMapper;

    public VmEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.sink = Sinks.many().multicast().directBestEffort();
    }

    @Override
    public void publishVmEvent(VmEventType eventType, VirtualMachine vm) {
        VmEventMessage message = new VmEventMessage(
                eventType.name(),
                new VmEventMessage.VmEventData(vm.getId(), vm.getName(), vm.getStatus().name())
        );

        try {
            String json = objectMapper.writeValueAsString(message);
            Sinks.EmitResult result = sink.tryEmitNext(json);
            if (result.isFailure()) {
                log.warn("Failed to emit VM event: {} - {}", eventType, result);
            } else {
                log.debug("VM event published: {} for VM id: {}", eventType, vm.getId());
            }
        } catch (Exception e) {
            log.error("Failed to serialize VM event: {}", eventType, e);
        }
    }

    public Flux<String> getEventFlux() {
        return sink.asFlux();
    }
}

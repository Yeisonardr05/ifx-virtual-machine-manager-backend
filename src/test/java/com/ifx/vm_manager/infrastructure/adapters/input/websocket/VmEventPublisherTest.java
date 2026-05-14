package com.ifx.vm_manager.infrastructure.adapters.input.websocket;

import com.ifx.vm_manager.domain.model.VirtualMachine;
import com.ifx.vm_manager.domain.model.VmEventType;
import com.ifx.vm_manager.domain.model.VmStatus;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VmEventPublisherTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void publishVmEvent_emitsJsonOnFlux() {
        VmEventPublisher publisher = new VmEventPublisher(jsonMapper);

        VirtualMachine vm = VirtualMachine.builder()
                .id(1L)
                .name("vm")
                .cores(1)
                .ram(1)
                .disk(1)
                .os("os")
                .status(VmStatus.STOPPED)
                .createdAt(null)
                .updatedAt(null)
                .build();

        StepVerifier.create(publisher.getEventFlux().take(1))
                .then(() -> publisher.publishVmEvent(VmEventType.VM_CREATED, vm))
                .assertNext(json -> assertThat(json).contains("VM_CREATED").contains("\"id\":1"))
                .verifyComplete();
    }

    @Test
    void publishVmEvent_swallowsSerializationErrors() throws Exception {
        tools.jackson.databind.ObjectMapper broken = mock(tools.jackson.databind.ObjectMapper.class);
        when(broken.writeValueAsString(any())).thenThrow(new RuntimeException("fail"));

        VmEventPublisher publisher = new VmEventPublisher(broken);
        VirtualMachine vm = VirtualMachine.builder()
                .id(2L)
                .name("vm")
                .cores(1)
                .ram(1)
                .disk(1)
                .os("os")
                .status(VmStatus.RUNNING)
                .createdAt(null)
                .updatedAt(null)
                .build();

        publisher.publishVmEvent(VmEventType.VM_UPDATED, vm);
    }
}

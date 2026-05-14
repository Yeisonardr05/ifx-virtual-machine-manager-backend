package com.ifx.vm_manager.infrastructure.adapters.input.rest;

import com.ifx.vm_manager.application.dto.response.ApiResponse;
import com.ifx.vm_manager.domain.model.VirtualMachine;
import com.ifx.vm_manager.domain.model.VmStatus;
import com.ifx.vm_manager.domain.ports.input.VmUseCase;
import com.ifx.vm_manager.infrastructure.exceptions.ValidationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VmHandlerTest {

    @Mock
    private VmUseCase vmUseCase;

    private Validator validator;
    private VmHandler handler;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        handler = new VmHandler(vmUseCase, validator);
        RouterFunction<ServerResponse> router = new VmRouter().vmRoutes(handler);
        client = WebTestClient.bindToRouterFunction(router).build();
    }

    @Test
    void createVm_returns201() {
        VirtualMachine created = VirtualMachine.builder()
                .id(10L)
                .name("MyVm")
                .cores(2)
                .ram(4)
                .disk(20)
                .os("Linux")
                .status(VmStatus.STOPPED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(vmUseCase.createVm(any(VirtualMachine.class))).thenReturn(Mono.just(created));

        client.post()
                .uri("/vms")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"MyVm","cores":2,"ram":4,"disk":20,"os":"Linux"}
                        """)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(ApiResponse.class)
                .value(body -> assertThat(body.success()).isTrue());

        verify(vmUseCase).createVm(any(VirtualMachine.class));
    }

    @Test
    void getAllVms_returnsList() {
        VirtualMachine vm = VirtualMachine.builder()
                .id(1L)
                .name("a")
                .cores(1)
                .ram(2)
                .disk(3)
                .os("os")
                .status(VmStatus.STOPPED)
                .createdAt(LocalDateTime.MIN)
                .updatedAt(LocalDateTime.MIN)
                .build();
        when(vmUseCase.getAllVms()).thenReturn(Flux.just(vm));

        client.get()
                .uri("/vms")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ApiResponse.class)
                .value(body -> assertThat(body.success()).isTrue());
    }

    @Test
    void getVmById_returnsVm() {
        VirtualMachine vm = VirtualMachine.builder()
                .id(5L)
                .name("a")
                .cores(1)
                .ram(2)
                .disk(3)
                .os("os")
                .status(VmStatus.RUNNING)
                .createdAt(LocalDateTime.MIN)
                .updatedAt(LocalDateTime.MIN)
                .build();
        when(vmUseCase.getVmById(5L)).thenReturn(Mono.just(vm));

        client.get()
                .uri("/vms/5")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ApiResponse.class)
                .value(body -> {
                    assertThat(body.success()).isTrue();
                    assertThat(body.data()).isNotNull();
                });
    }

    @Test
    void updateVm_returnsOk() {
        VirtualMachine updated = VirtualMachine.builder()
                .id(3L)
                .name("n")
                .cores(2)
                .ram(4)
                .disk(8)
                .os("os")
                .status(VmStatus.STOPPED)
                .createdAt(LocalDateTime.MIN)
                .updatedAt(LocalDateTime.MIN)
                .build();
        when(vmUseCase.updateVm(eq(3L), any(VirtualMachine.class))).thenReturn(Mono.just(updated));

        client.put()
                .uri("/vms/3")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"MyVm","cores":2,"ram":4,"disk":8,"os":"Linux","status":"STOPPED"}
                        """)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void deleteVm_returnsOk() {
        when(vmUseCase.deleteVm(7L)).thenReturn(Mono.empty());

        client.delete().uri("/vms/7").exchange().expectStatus().isOk();
    }

    @Test
    void getVmById_invalidId_throwsValidationException() {
        ServerRequest request = MockServerRequest.builder()
                .method(HttpMethod.GET)
                .uri(URI.create("http://localhost/vms/abc"))
                .pathVariable("id", "abc")
                .build();

        assertThrows(ValidationException.class, () -> handler.getVmById(request));
    }
}

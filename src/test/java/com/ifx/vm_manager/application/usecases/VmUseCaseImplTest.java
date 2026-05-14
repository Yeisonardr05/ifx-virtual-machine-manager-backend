package com.ifx.vm_manager.application.usecases;

import com.ifx.vm_manager.domain.model.VirtualMachine;
import com.ifx.vm_manager.domain.model.VmEventType;
import com.ifx.vm_manager.domain.model.VmStatus;
import com.ifx.vm_manager.domain.ports.output.VmEventPort;
import com.ifx.vm_manager.domain.ports.output.VmRepositoryPort;
import com.ifx.vm_manager.infrastructure.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VmUseCaseImplTest {

    @Mock
    private VmRepositoryPort vmRepositoryPort;

    @Mock
    private VmEventPort vmEventPort;

    private VmUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new VmUseCaseImpl(vmRepositoryPort, vmEventPort);
    }

    @Test
    void createVm_setsStoppedAndPublishesCreated() {
        VirtualMachine input = sampleVm(null, VmStatus.RUNNING);
        VirtualMachine saved = sampleVm(1L, VmStatus.STOPPED);

        when(vmRepositoryPort.save(any(VirtualMachine.class))).thenAnswer(inv -> Mono.just(saved));

        StepVerifier.create(useCase.createVm(input))
                .expectNextMatches(vm -> vm.getId().equals(1L) && vm.getStatus() == VmStatus.STOPPED)
                .verifyComplete();

        ArgumentCaptor<VirtualMachine> captor = ArgumentCaptor.forClass(VirtualMachine.class);
        verify(vmRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(VmStatus.STOPPED);
        verify(vmEventPort).publishVmEvent(eq(VmEventType.VM_CREATED), eq(saved));
    }

    @Test
    void getAllVms_delegatesToRepository() {
        VirtualMachine a = sampleVm(1L, VmStatus.STOPPED);
        when(vmRepositoryPort.findAll()).thenReturn(Flux.just(a));

        StepVerifier.create(useCase.getAllVms())
                .expectNext(a)
                .verifyComplete();
    }

    @Test
    void getVmById_found() {
        VirtualMachine vm = sampleVm(5L, VmStatus.STOPPED);
        when(vmRepositoryPort.findById(5L)).thenReturn(Mono.just(vm));

        StepVerifier.create(useCase.getVmById(5L))
                .expectNext(vm)
                .verifyComplete();
    }

    @Test
    void getVmById_notFound() {
        when(vmRepositoryPort.findById(9L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getVmById(9L))
                .verifyErrorSatisfies(ex -> assertThat(ex).isInstanceOf(NotFoundException.class));
    }

    @Test
    void updateVm_mergesFieldsAndPreservesStatusWhenNull() {
        VirtualMachine existing = sampleVm(2L, VmStatus.RUNNING);
        VirtualMachine patch = VirtualMachine.builder()
                .name("new-name")
                .cores(4)
                .ram(8)
                .disk(100)
                .os("linux")
                .status(null)
                .build();

        when(vmRepositoryPort.findById(2L)).thenReturn(Mono.just(existing));
        when(vmRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.updateVm(2L, patch))
                .assertNext(updated -> {
                    assertThat(updated.getName()).isEqualTo("new-name");
                    assertThat(updated.getCores()).isEqualTo(4);
                    assertThat(updated.getStatus()).isEqualTo(VmStatus.RUNNING);
                })
                .verifyComplete();

        verify(vmEventPort).publishVmEvent(eq(VmEventType.VM_UPDATED), any(VirtualMachine.class));
    }

    @Test
    void updateVm_appliesNewStatusWhenProvided() {
        VirtualMachine existing = sampleVm(2L, VmStatus.STOPPED);
        VirtualMachine patch = VirtualMachine.builder()
                .name("n")
                .cores(2)
                .ram(4)
                .disk(20)
                .os("win")
                .status(VmStatus.PAUSED)
                .build();

        when(vmRepositoryPort.findById(2L)).thenReturn(Mono.just(existing));
        when(vmRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.updateVm(2L, patch))
                .assertNext(updated -> assertThat(updated.getStatus()).isEqualTo(VmStatus.PAUSED))
                .verifyComplete();
    }

    @Test
    void updateVm_notFound() {
        when(vmRepositoryPort.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(
                        useCase.updateVm(1L, VirtualMachine.builder().name("x").cores(1).ram(1).disk(1).os("o").build()))
                .verifyError(NotFoundException.class);

        verify(vmEventPort, never()).publishVmEvent(any(), any());
    }

    @Test
    void deleteVm_publishesDeletedThenRemoves() {
        VirtualMachine vm = sampleVm(3L, VmStatus.STOPPED);
        when(vmRepositoryPort.findById(3L)).thenReturn(Mono.just(vm));
        when(vmRepositoryPort.deleteById(3L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.deleteVm(3L)).verifyComplete();

        verify(vmEventPort).publishVmEvent(eq(VmEventType.VM_DELETED), eq(vm));
        verify(vmRepositoryPort).deleteById(3L);
    }

    @Test
    void deleteVm_notFound() {
        when(vmRepositoryPort.findById(3L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.deleteVm(3L)).verifyError(NotFoundException.class);
    }

    @Test
    void updateVmStatus_changesStatusAndPublishes() {
        VirtualMachine existing = sampleVm(4L, VmStatus.STOPPED);
        when(vmRepositoryPort.findById(4L)).thenReturn(Mono.just(existing));
        when(vmRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.updateVmStatus(4L, VmStatus.RUNNING))
                .assertNext(vm -> assertThat(vm.getStatus()).isEqualTo(VmStatus.RUNNING))
                .verifyComplete();

        verify(vmEventPort).publishVmEvent(eq(VmEventType.VM_STATUS_CHANGED), any(VirtualMachine.class));
    }

    @Test
    void updateVmStatus_notFound() {
        when(vmRepositoryPort.findById(4L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateVmStatus(4L, VmStatus.RUNNING))
                .verifyError(NotFoundException.class);
    }

    private static VirtualMachine sampleVm(Long id, VmStatus status) {
        return VirtualMachine.builder()
                .id(id)
                .name("vm")
                .cores(2)
                .ram(4)
                .disk(10)
                .os("linux")
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}

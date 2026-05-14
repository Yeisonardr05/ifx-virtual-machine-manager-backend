package com.ifx.vm_manager.application.usecases;

import com.ifx.vm_manager.domain.model.VirtualMachine;
import com.ifx.vm_manager.domain.model.VmEventType;
import com.ifx.vm_manager.domain.model.VmStatus;
import com.ifx.vm_manager.domain.ports.input.VmUseCase;
import com.ifx.vm_manager.domain.ports.output.VmEventPort;
import com.ifx.vm_manager.domain.ports.output.VmRepositoryPort;
import com.ifx.vm_manager.infrastructure.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class VmUseCaseImpl implements VmUseCase {

    private final VmRepositoryPort vmRepositoryPort;
    private final VmEventPort vmEventPort;

    @Override
    @Transactional
    public Mono<VirtualMachine> createVm(VirtualMachine vm) {
        VirtualMachine vmToCreate = vm.toBuilder()
                .status(VmStatus.STOPPED)
                .build();

        return vmRepositoryPort.save(vmToCreate)
                .doOnSuccess(created -> {
                    log.info("VM created with id: {}", created.getId());
                    vmEventPort.publishVmEvent(VmEventType.VM_CREATED, created);
                });
    }

    @Override
    public Flux<VirtualMachine> getAllVms() {
        log.debug("Retrieving all VMs");
        return vmRepositoryPort.findAll();
    }

    @Override
    public Mono<VirtualMachine> getVmById(Long id) {
        log.debug("Retrieving VM by id: {}", id);
        return vmRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("VM not found with id: " + id)));
    }

    @Override
    @Transactional
    public Mono<VirtualMachine> updateVm(Long id, VirtualMachine updatedVm) {
        return vmRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("VM not found with id: " + id)))
                .flatMap(existing -> {
                    VirtualMachine toSave = existing.toBuilder()
                            .name(updatedVm.getName())
                            .cores(updatedVm.getCores())
                            .ram(updatedVm.getRam())
                            .disk(updatedVm.getDisk())
                            .os(updatedVm.getOs())
                            .status(updatedVm.getStatus() != null ? updatedVm.getStatus() : existing.getStatus())
                            .build();
                    return vmRepositoryPort.save(toSave);
                })
                .doOnSuccess(updated -> {
                    log.info("VM updated with id: {}", updated.getId());
                    vmEventPort.publishVmEvent(VmEventType.VM_UPDATED, updated);
                });
    }

    @Override
    @Transactional
    public Mono<Void> deleteVm(Long id) {
        return vmRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("VM not found with id: " + id)))
                .flatMap(vm -> {
                    vmEventPort.publishVmEvent(VmEventType.VM_DELETED, vm);
                    return vmRepositoryPort.deleteById(id);
                })
                .doOnSuccess(v -> log.info("VM deleted with id: {}", id));
    }

    @Override
    @Transactional
    public Mono<VirtualMachine> updateVmStatus(Long id, VmStatus status) {
        return vmRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("VM not found with id: " + id)))
                .flatMap(existing -> {
                    VirtualMachine updated = existing.toBuilder().status(status).build();
                    return vmRepositoryPort.save(updated);
                })
                .doOnSuccess(updated -> {
                    log.info("VM status updated - id: {}, status: {}", id, status);
                    vmEventPort.publishVmEvent(VmEventType.VM_STATUS_CHANGED, updated);
                });
    }
}

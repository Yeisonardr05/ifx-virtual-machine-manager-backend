package com.ifx.vm_manager.domain.ports.input;

import com.ifx.vm_manager.domain.model.VirtualMachine;
import com.ifx.vm_manager.domain.model.VmStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VmUseCase {

    Mono<VirtualMachine> createVm(VirtualMachine vm);

    Flux<VirtualMachine> getAllVms();

    Mono<VirtualMachine> getVmById(Long id);

    Mono<VirtualMachine> updateVm(Long id, VirtualMachine vm);

    Mono<Void> deleteVm(Long id);

    Mono<VirtualMachine> updateVmStatus(Long id, VmStatus status);
}

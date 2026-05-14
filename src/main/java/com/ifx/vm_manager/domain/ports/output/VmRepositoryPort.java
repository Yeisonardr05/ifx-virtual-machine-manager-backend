package com.ifx.vm_manager.domain.ports.output;

import com.ifx.vm_manager.domain.model.VirtualMachine;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VmRepositoryPort {

    Mono<VirtualMachine> save(VirtualMachine vm);

    Flux<VirtualMachine> findAll();

    Mono<VirtualMachine> findById(Long id);

    Mono<Void> deleteById(Long id);
}

package com.ifx.vm_manager.infrastructure.adapters.output.persistence;

import com.ifx.vm_manager.domain.model.VirtualMachine;
import com.ifx.vm_manager.domain.ports.output.VmRepositoryPort;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.mapper.VmEntityMapper;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.repository.VmR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class VmRepositoryAdapter implements VmRepositoryPort {

    private final VmR2dbcRepository r2dbcRepository;
    private final VmEntityMapper mapper;

    @Override
    public Mono<VirtualMachine> save(VirtualMachine vm) {
        return r2dbcRepository.save(mapper.toEntity(vm))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<VirtualMachine> findAll() {
        return r2dbcRepository.findAll()
                .map(mapper::toDomain);
    }

    @Override
    public Mono<VirtualMachine> findById(Long id) {
        return r2dbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return r2dbcRepository.deleteById(id);
    }
}

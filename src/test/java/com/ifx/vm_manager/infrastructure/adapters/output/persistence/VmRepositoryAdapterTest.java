package com.ifx.vm_manager.infrastructure.adapters.output.persistence;

import com.ifx.vm_manager.domain.model.VirtualMachine;
import com.ifx.vm_manager.domain.model.VmStatus;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.entity.VmEntity;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.mapper.VmEntityMapper;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.repository.VmR2dbcRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VmRepositoryAdapterTest {

    @Mock
    private VmR2dbcRepository r2dbcRepository;

    @Mock
    private VmEntityMapper mapper;

    @InjectMocks
    private VmRepositoryAdapter adapter;

    @Test
    void save_roundTripsThroughMapper() {
        VirtualMachine domain = VirtualMachine.builder()
                .id(1L)
                .name("n")
                .cores(2)
                .ram(4)
                .disk(8)
                .os("os")
                .status(VmStatus.STOPPED)
                .createdAt(LocalDateTime.MIN)
                .updatedAt(LocalDateTime.MIN)
                .build();
        VmEntity entity = new VmEntity();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(r2dbcRepository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.save(domain)).expectNext(domain).verifyComplete();
    }

    @Test
    void findAll_mapsEachRow() {
        VmEntity e = new VmEntity();
        VirtualMachine vm = VirtualMachine.builder()
                .id(1L)
                .name("n")
                .cores(1)
                .ram(1)
                .disk(1)
                .os("o")
                .status(VmStatus.STOPPED)
                .createdAt(null)
                .updatedAt(null)
                .build();
        when(r2dbcRepository.findAll()).thenReturn(Flux.just(e));
        when(mapper.toDomain(e)).thenReturn(vm);

        StepVerifier.create(adapter.findAll()).expectNext(vm).verifyComplete();
    }

    @Test
    void findById_delegates() {
        VmEntity e = new VmEntity();
        VirtualMachine vm = VirtualMachine.builder()
                .id(5L)
                .name("n")
                .cores(1)
                .ram(1)
                .disk(1)
                .os("o")
                .status(VmStatus.STOPPED)
                .createdAt(null)
                .updatedAt(null)
                .build();
        when(r2dbcRepository.findById(5L)).thenReturn(Mono.just(e));
        when(mapper.toDomain(e)).thenReturn(vm);

        StepVerifier.create(adapter.findById(5L)).expectNext(vm).verifyComplete();
    }

    @Test
    void deleteById_delegates() {
        when(r2dbcRepository.deleteById(7L)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(7L)).verifyComplete();
        verify(r2dbcRepository).deleteById(7L);
    }
}

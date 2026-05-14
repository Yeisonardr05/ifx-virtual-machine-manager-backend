package com.ifx.vm_manager.infrastructure.adapters.output.persistence.mapper;

import com.ifx.vm_manager.domain.model.VirtualMachine;
import com.ifx.vm_manager.domain.model.VmStatus;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.entity.VmEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class VmEntityMapperTest {

    private final VmEntityMapper mapper = new VmEntityMapper();

    @Test
    void toDomain_mapsAllFields() {
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 10, 0);
        VmEntity entity = new VmEntity(1L, "n", 2, 4, 10, "os", "STOPPED", created, updated);

        VirtualMachine vm = mapper.toDomain(entity);

        assertThat(vm.getId()).isEqualTo(1L);
        assertThat(vm.getName()).isEqualTo("n");
        assertThat(vm.getCores()).isEqualTo(2);
        assertThat(vm.getRam()).isEqualTo(4);
        assertThat(vm.getDisk()).isEqualTo(10);
        assertThat(vm.getOs()).isEqualTo("os");
        assertThat(vm.getStatus()).isEqualTo(VmStatus.STOPPED);
        assertThat(vm.getCreatedAt()).isEqualTo(created);
        assertThat(vm.getUpdatedAt()).isEqualTo(updated);
    }

    @Test
    void toEntity_defaultsStatusWhenNull() {
        VirtualMachine vm = VirtualMachine.builder()
                .id(9L)
                .name("x")
                .cores(1)
                .ram(2)
                .disk(3)
                .os("o")
                .status(null)
                .createdAt(null)
                .updatedAt(null)
                .build();

        VmEntity entity = mapper.toEntity(vm);

        assertThat(entity.getStatus()).isEqualTo(VmStatus.STOPPED.name());
        assertThat(entity.getName()).isEqualTo("x");
    }

    @Test
    void toEntity_usesExplicitStatus() {
        VirtualMachine vm = VirtualMachine.builder()
                .id(1L)
                .name("x")
                .cores(1)
                .ram(2)
                .disk(3)
                .os("o")
                .status(VmStatus.RUNNING)
                .createdAt(null)
                .updatedAt(null)
                .build();

        assertThat(mapper.toEntity(vm).getStatus()).isEqualTo("RUNNING");
    }
}

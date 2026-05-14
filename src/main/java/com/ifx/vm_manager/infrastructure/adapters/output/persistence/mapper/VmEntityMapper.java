package com.ifx.vm_manager.infrastructure.adapters.output.persistence.mapper;

import com.ifx.vm_manager.domain.model.VirtualMachine;
import com.ifx.vm_manager.domain.model.VmStatus;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.entity.VmEntity;
import org.springframework.stereotype.Component;

@Component
public class VmEntityMapper {

    public VirtualMachine toDomain(VmEntity entity) {
        return VirtualMachine.builder()
                .id(entity.getId())
                .name(entity.getName())
                .cores(entity.getCores())
                .ram(entity.getRam())
                .disk(entity.getDisk())
                .os(entity.getOs())
                .status(VmStatus.valueOf(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public VmEntity toEntity(VirtualMachine vm) {
        VmEntity entity = new VmEntity();
        entity.setId(vm.getId());
        entity.setName(vm.getName());
        entity.setCores(vm.getCores());
        entity.setRam(vm.getRam());
        entity.setDisk(vm.getDisk());
        entity.setOs(vm.getOs());
        entity.setStatus(vm.getStatus() != null ? vm.getStatus().name() : VmStatus.STOPPED.name());
        entity.setCreatedAt(vm.getCreatedAt());
        entity.setUpdatedAt(vm.getUpdatedAt());
        return entity;
    }
}

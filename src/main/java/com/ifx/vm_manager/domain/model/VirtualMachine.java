package com.ifx.vm_manager.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class VirtualMachine {
    Long id;
    String name;
    Integer cores;
    Integer ram;
    Integer disk;
    String os;
    VmStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

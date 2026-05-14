package com.ifx.vm_manager.application.dto.response;

import java.time.LocalDateTime;

public record VmResponse(
        Long id,
        String name,
        Integer cores,
        Integer ram,
        Integer disk,
        String os,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

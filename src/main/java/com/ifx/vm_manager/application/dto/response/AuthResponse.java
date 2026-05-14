package com.ifx.vm_manager.application.dto.response;

public record AuthResponse(
        Long id,
        String name,
        String email,
        String role
) {}

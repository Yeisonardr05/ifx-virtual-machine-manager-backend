package com.ifx.vm_manager.application.dto.response;

public record VmEventMessage(
        String event,
        VmEventData data
) {
    public record VmEventData(
            Long id,
            String name,
            String status
    ) {}
}

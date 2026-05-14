package com.ifx.vm_manager.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateVmRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotNull(message = "Cores is required")
        @Min(value = 1, message = "Cores must be at least 1")
        Integer cores,

        @NotNull(message = "RAM is required")
        @Min(value = 1, message = "RAM must be at least 1 GB")
        @Max(value = 64, message = "RAM must be at most 64 GB")
        Integer ram,

        @NotNull(message = "Disk is required")
        @Min(value = 1, message = "Disk must be at least 1 GB")
        Integer disk,

        @NotBlank(message = "OS is required")
        @Size(max = 100, message = "OS name must not exceed 100 characters")
        String os
) {}

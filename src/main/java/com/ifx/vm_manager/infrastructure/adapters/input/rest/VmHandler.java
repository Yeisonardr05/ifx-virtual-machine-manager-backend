package com.ifx.vm_manager.infrastructure.adapters.input.rest;

import com.ifx.vm_manager.application.dto.request.CreateVmRequest;
import com.ifx.vm_manager.application.dto.request.UpdateVmRequest;
import com.ifx.vm_manager.application.dto.response.ApiResponse;
import com.ifx.vm_manager.application.dto.response.VmResponse;
import com.ifx.vm_manager.domain.model.VirtualMachine;
import com.ifx.vm_manager.domain.model.VmStatus;
import com.ifx.vm_manager.domain.ports.input.VmUseCase;
import com.ifx.vm_manager.infrastructure.exceptions.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class VmHandler {

    private final VmUseCase vmUseCase;
    private final Validator validator;

    public Mono<ServerResponse> createVm(ServerRequest request) {
        return request.bodyToMono(CreateVmRequest.class)
                .flatMap(this::validate)
                .map(this::toNewVirtualMachine)
                .flatMap(vmUseCase::createVm)
                .map(this::toVmResponse)
                .flatMap(vm -> ServerResponse.status(HttpStatus.CREATED)
                        .bodyValue(ApiResponse.created("VM created successfully", vm)));
    }

    public Mono<ServerResponse> getAllVms(ServerRequest request) {
        return vmUseCase.getAllVms()
                .map(this::toVmResponse)
                .collectList()
                .flatMap(vms -> ServerResponse.ok()
                        .bodyValue(ApiResponse.success(vms)));
    }

    public Mono<ServerResponse> getVmById(ServerRequest request) {
        Long id = extractId(request);
        return vmUseCase.getVmById(id)
                .map(this::toVmResponse)
                .flatMap(vm -> ServerResponse.ok()
                        .bodyValue(ApiResponse.success(vm)));
    }

    public Mono<ServerResponse> updateVm(ServerRequest request) {
        Long id = extractId(request);
        return request.bodyToMono(UpdateVmRequest.class)
                .flatMap(this::validate)
                .map(this::toUpdatedVirtualMachine)
                .flatMap(vm -> vmUseCase.updateVm(id, vm))
                .map(this::toVmResponse)
                .flatMap(vm -> ServerResponse.ok()
                        .bodyValue(ApiResponse.success("VM updated successfully", vm)));
    }

    public Mono<ServerResponse> deleteVm(ServerRequest request) {
        Long id = extractId(request);
        return vmUseCase.deleteVm(id)
                .then(ServerResponse.ok()
                        .bodyValue(ApiResponse.success("VM deleted successfully", null)));
    }

    private VirtualMachine toNewVirtualMachine(CreateVmRequest req) {
        return VirtualMachine.builder()
                .name(req.name())
                .cores(req.cores())
                .ram(req.ram())
                .disk(req.disk())
                .os(req.os())
                .status(VmStatus.STOPPED)
                .build();
    }

    private VirtualMachine toUpdatedVirtualMachine(UpdateVmRequest req) {
        return VirtualMachine.builder()
                .name(req.name())
                .cores(req.cores())
                .ram(req.ram())
                .disk(req.disk())
                .os(req.os())
                .status(req.status() != null ? VmStatus.valueOf(req.status()) : null)
                .build();
    }

    private VmResponse toVmResponse(VirtualMachine vm) {
        return new VmResponse(
                vm.getId(),
                vm.getName(),
                vm.getCores(),
                vm.getRam(),
                vm.getDisk(),
                vm.getOs(),
                vm.getStatus().name(),
                vm.getCreatedAt(),
                vm.getUpdatedAt()
        );
    }

    private Long extractId(ServerRequest request) {
        try {
            return Long.parseLong(request.pathVariable("id"));
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid VM id: must be a number");
        }
    }

    private <T> Mono<T> validate(T body) {
        Set<ConstraintViolation<T>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            return Mono.error(new ValidationException(message));
        }
        return Mono.just(body);
    }
}

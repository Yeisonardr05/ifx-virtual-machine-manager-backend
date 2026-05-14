package com.ifx.vm_manager.infrastructure.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BusinessException extends ResponseStatusException {

    public BusinessException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }
}

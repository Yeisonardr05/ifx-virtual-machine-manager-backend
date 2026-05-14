package com.ifx.vm_manager.domain.ports.input;

import com.ifx.vm_manager.domain.model.User;
import reactor.core.publisher.Mono;

public interface AuthUseCase {

    Mono<User> authenticate(String email, String password);
}

package com.ifx.vm_manager.domain.ports.output;

import com.ifx.vm_manager.domain.model.User;
import reactor.core.publisher.Mono;

public interface UserRepositoryPort {

    Mono<User> findByEmail(String email);

    Mono<User> save(User user);
}

package com.ifx.vm_manager.infrastructure.adapters.output.persistence;

import com.ifx.vm_manager.domain.model.User;
import com.ifx.vm_manager.domain.ports.output.UserRepositoryPort;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.mapper.UserEntityMapper;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.repository.UserR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserR2dbcRepository r2dbcRepository;
    private final UserEntityMapper mapper;

    @Override
    public Mono<User> findByEmail(String email) {
        return r2dbcRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<User> save(User user) {
        return r2dbcRepository.save(mapper.toEntity(user))
                .map(mapper::toDomain);
    }
}

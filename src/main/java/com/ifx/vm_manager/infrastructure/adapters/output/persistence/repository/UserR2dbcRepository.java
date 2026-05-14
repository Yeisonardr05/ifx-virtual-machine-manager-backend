package com.ifx.vm_manager.infrastructure.adapters.output.persistence.repository;

import com.ifx.vm_manager.infrastructure.adapters.output.persistence.entity.UserEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserR2dbcRepository extends ReactiveCrudRepository<UserEntity, Long> {

    Mono<UserEntity> findByEmail(String email);
}

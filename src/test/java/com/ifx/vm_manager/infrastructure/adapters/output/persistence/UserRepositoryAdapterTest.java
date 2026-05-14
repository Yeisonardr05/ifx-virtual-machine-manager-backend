package com.ifx.vm_manager.infrastructure.adapters.output.persistence;

import com.ifx.vm_manager.domain.model.Role;
import com.ifx.vm_manager.domain.model.User;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.entity.UserEntity;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.mapper.UserEntityMapper;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.repository.UserR2dbcRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private UserR2dbcRepository r2dbcRepository;

    @Mock
    private UserEntityMapper mapper;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Test
    void findByEmail_mapsToDomain() {
        UserEntity entity = new UserEntity();
        User user = User.builder().id(1L).name("n").email("e").password("p").role(Role.ADMIN).build();
        when(r2dbcRepository.findByEmail("e")).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(user);

        StepVerifier.create(adapter.findByEmail("e")).expectNext(user).verifyComplete();
    }

    @Test
    void save_roundTripsThroughMapper() {
        User user = User.builder().id(2L).name("n").email("e").password("p").role(Role.CLIENT).build();
        UserEntity entity = new UserEntity();
        when(mapper.toEntity(user)).thenReturn(entity);
        when(r2dbcRepository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(user);

        StepVerifier.create(adapter.save(user)).expectNext(user).verifyComplete();
    }
}

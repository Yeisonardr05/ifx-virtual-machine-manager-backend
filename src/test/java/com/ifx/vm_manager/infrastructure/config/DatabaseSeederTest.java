package com.ifx.vm_manager.infrastructure.config;

import com.ifx.vm_manager.infrastructure.adapters.output.persistence.entity.UserEntity;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.repository.UserR2dbcRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseSeederTest {

    @Mock
    private UserR2dbcRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DatabaseSeeder seeder;

    @Test
    void seedDefaultUsers_doesNothingWhenUsersExist() throws Exception {
        when(userRepository.count()).thenReturn(Mono.just(2L));

        seeder.seedDefaultUsers();

        Thread.sleep(50);
        verify(userRepository, never()).save(any());
    }

    @Test
    void seedDefaultUsers_insertsDefaultsWhenEmpty() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        when(userRepository.count()).thenReturn(Mono.just(0L));
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    latch.countDown();
                    return Mono.just(invocation.getArgument(0));
                });

        seeder.seedDefaultUsers();

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
    }
}

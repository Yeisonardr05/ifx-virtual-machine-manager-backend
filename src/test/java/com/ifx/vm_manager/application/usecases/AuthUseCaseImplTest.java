package com.ifx.vm_manager.application.usecases;

import com.ifx.vm_manager.domain.model.Role;
import com.ifx.vm_manager.domain.model.User;
import com.ifx.vm_manager.domain.ports.output.UserRepositoryPort;
import com.ifx.vm_manager.infrastructure.exceptions.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new AuthUseCaseImpl(userRepositoryPort, passwordEncoder);
    }

    @Test
    void authenticate_success() {
        User user = User.builder()
                .id(1L)
                .name("Test")
                .email("a@b.com")
                .password("hash")
                .role(Role.ADMIN)
                .build();

        when(userRepositoryPort.findByEmail("a@b.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);

        StepVerifier.create(useCase.authenticate("a@b.com", "secret"))
                .assertNext(u -> assertThat(u.getEmail()).isEqualTo("a@b.com"))
                .verifyComplete();
    }

    @Test
    void authenticate_userNotFound() {
        when(userRepositoryPort.findByEmail("x@y.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.authenticate("x@y.com", "secret"))
                .verifyErrorSatisfies(ex -> assertThat(ex).isInstanceOf(UnauthorizedException.class));
    }

    @Test
    void authenticate_badPassword() {
        User user = User.builder()
                .id(1L)
                .name("Test")
                .email("a@b.com")
                .password("hash")
                .role(Role.CLIENT)
                .build();

        when(userRepositoryPort.findByEmail("a@b.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        StepVerifier.create(useCase.authenticate("a@b.com", "wrong"))
                .verifyError(UnauthorizedException.class);
    }
}

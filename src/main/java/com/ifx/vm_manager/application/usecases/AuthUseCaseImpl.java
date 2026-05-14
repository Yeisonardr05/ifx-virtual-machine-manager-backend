package com.ifx.vm_manager.application.usecases;

import com.ifx.vm_manager.domain.model.User;
import com.ifx.vm_manager.domain.ports.input.AuthUseCase;
import com.ifx.vm_manager.domain.ports.output.UserRepositoryPort;
import com.ifx.vm_manager.infrastructure.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUseCaseImpl implements AuthUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> authenticate(String email, String password) {
        log.debug("Authenticating user: {}", email);

        return userRepositoryPort.findByEmail(email)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Invalid credentials")))
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .switchIfEmpty(Mono.error(new UnauthorizedException("Invalid credentials")))
                .doOnSuccess(user -> log.info("User authenticated successfully: {}", user.getEmail()));
    }
}

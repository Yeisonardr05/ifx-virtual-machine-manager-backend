package com.ifx.vm_manager.infrastructure.config;

import com.ifx.vm_manager.infrastructure.adapters.output.persistence.entity.UserEntity;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.repository.UserR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder {

    private final UserR2dbcRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void seedDefaultUsers() {
        userRepository.count()
                .filter(count -> count == 0)
                .flatMapMany(count -> {
                    log.info("No users found. Seeding default users...");
                    return Flux.fromIterable(buildDefaultUsers());
                })
                .flatMap(userRepository::save)
                .doOnNext(user -> log.info("Seeded user: {} ({})", user.getEmail(), user.getRole()))
                .doOnComplete(() -> log.info("Database seeding completed"))
                .subscribe(
                        null,
                        error -> log.error("Error during database seeding: {}", error.getMessage())
                );
    }

    private List<UserEntity> buildDefaultUsers() {
        UserEntity admin = new UserEntity();
        admin.setName("Admin User");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRole("ADMIN");

        UserEntity client = new UserEntity();
        client.setName("Client User");
        client.setEmail("client@test.com");
        client.setPassword(passwordEncoder.encode("client123"));
        client.setRole("CLIENT");

        return List.of(admin, client);
    }
}

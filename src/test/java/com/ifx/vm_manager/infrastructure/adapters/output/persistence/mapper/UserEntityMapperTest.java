package com.ifx.vm_manager.infrastructure.adapters.output.persistence.mapper;

import com.ifx.vm_manager.domain.model.Role;
import com.ifx.vm_manager.domain.model.User;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityMapperTest {

    private final UserEntityMapper mapper = new UserEntityMapper();

    @Test
    void toDomain_mapsFields() {
        LocalDateTime created = LocalDateTime.of(2024, 3, 3, 12, 0);
        UserEntity entity = new UserEntity(1L, "N", "e@e.com", "p", "ADMIN", created);

        User user = mapper.toDomain(entity);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("N");
        assertThat(user.getEmail()).isEqualTo("e@e.com");
        assertThat(user.getPassword()).isEqualTo("p");
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void toEntity_mapsRoleName() {
        User user = User.builder()
                .id(2L)
                .name("U")
                .email("u@u.com")
                .password("pw")
                .role(Role.CLIENT)
                .build();

        UserEntity entity = mapper.toEntity(user);

        assertThat(entity.getRole()).isEqualTo("CLIENT");
        assertThat(entity.getEmail()).isEqualTo("u@u.com");
    }
}

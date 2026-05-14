package com.ifx.vm_manager.infrastructure.adapters.output.persistence.mapper;

import com.ifx.vm_manager.domain.model.Role;
import com.ifx.vm_manager.domain.model.User;
import com.ifx.vm_manager.infrastructure.adapters.output.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .role(Role.valueOf(entity.getRole()))
                .build();
    }

    public UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setRole(user.getRole().name());
        return entity;
    }
}

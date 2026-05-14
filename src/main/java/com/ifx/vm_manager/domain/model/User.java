package com.ifx.vm_manager.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class User {
    Long id;
    String name;
    String email;
    String password;
    Role role;
}

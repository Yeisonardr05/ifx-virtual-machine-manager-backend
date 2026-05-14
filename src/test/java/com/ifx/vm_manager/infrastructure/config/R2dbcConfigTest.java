package com.ifx.vm_manager.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class R2dbcConfigTest {

    @Test
    void canInstantiate() {
        assertThat(new R2dbcConfig()).isNotNull();
    }
}

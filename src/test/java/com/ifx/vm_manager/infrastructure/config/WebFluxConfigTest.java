package com.ifx.vm_manager.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerCodecConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

class WebFluxConfigTest {

    @Test
    void configuresCodecs() {
        WebFluxConfig config = new WebFluxConfig();
        ServerCodecConfigurer configurer = ServerCodecConfigurer.create();
        config.configureHttpMessageCodecs(configurer);

        assertThat(configurer.getReaders()).isNotEmpty();
    }
}

package com.ifx.vm_manager.infrastructure.config;

import com.ifx.vm_manager.infrastructure.adapters.output.security.JwtSecurityContextRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void corsConfigurationSource_parsesOrigins() {
        JwtSecurityContextRepository repository = mock(JwtSecurityContextRepository.class);
        SecurityConfig config = new SecurityConfig(repository, JsonMapper.builder().build());
        ReflectionTestUtils.setField(config, "allowedOriginsConfig", "http://a.example,http://b.example");

        CorsConfigurationSource source = config.corsConfigurationSource();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").header("Origin", "http://a.example"));
        CorsConfiguration cors = source.getCorsConfiguration(exchange);

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactly("http://a.example", "http://b.example");
    }

    @Test
    void securityWebFilterChain_builds() {
        JwtSecurityContextRepository repository = mock(JwtSecurityContextRepository.class);
        SecurityConfig config = new SecurityConfig(repository, JsonMapper.builder().build());
        ReflectionTestUtils.setField(config, "allowedOriginsConfig", "http://localhost:3000");

        ServerHttpSecurity http = ServerHttpSecurity.http();
        SecurityWebFilterChain chain = config.securityWebFilterChain(http);

        assertThat(chain).isNotNull();
    }
}

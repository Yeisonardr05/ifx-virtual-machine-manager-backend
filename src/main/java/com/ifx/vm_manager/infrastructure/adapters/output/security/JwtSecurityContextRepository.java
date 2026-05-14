package com.ifx.vm_manager.infrastructure.adapters.output.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

    private static final String AUTH_COOKIE_NAME = "auth-token";

    private final JwtService jwtService;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(AUTH_COOKIE_NAME))
                .map(HttpCookie::getValue)
                .filter(jwtService::isTokenValid)
                .map(this::buildSecurityContext)
                .doOnError(e -> log.debug("Failed to load security context: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    private SecurityContext buildSecurityContext(String token) {
        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email, token, authorities
        );

        return new SecurityContextImpl(authentication);
    }
}

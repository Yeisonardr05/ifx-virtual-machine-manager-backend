package com.ifx.vm_manager.infrastructure.adapters.input.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class VmRouter {

    @Bean
    public RouterFunction<ServerResponse> vmRoutes(VmHandler handler) {
        return RouterFunctions
                .route(POST("/vms").and(accept(APPLICATION_JSON)), handler::createVm)
                .andRoute(GET("/vms"), handler::getAllVms)
                .andRoute(GET("/vms/{id}"), handler::getVmById)
                .andRoute(PUT("/vms/{id}").and(accept(APPLICATION_JSON)), handler::updateVm)
                .andRoute(DELETE("/vms/{id}"), handler::deleteVm);
    }
}

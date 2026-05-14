package com.ifx.vm_manager.infrastructure.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void of_populatesFields() {
        ErrorResponse r = ErrorResponse.of(404, "NOT_FOUND", "missing", "/vms/1");

        assertThat(r.status()).isEqualTo(404);
        assertThat(r.error()).isEqualTo("NOT_FOUND");
        assertThat(r.message()).isEqualTo("missing");
        assertThat(r.path()).isEqualTo("/vms/1");
        assertThat(r.timestamp()).isNotNull();
    }
}

package com.ifx.vm_manager.application.dto.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void success_withoutMessage() {
        ApiResponse<List<String>> r = ApiResponse.success(List.of("a"));
        assertThat(r.success()).isTrue();
        assertThat(r.message()).isEqualTo("Success");
        assertThat(r.data()).containsExactly("a");
        assertThat(r.timestamp()).isNotNull();
    }

    @Test
    void success_withMessage() {
        ApiResponse<String> r = ApiResponse.success("done", "x");
        assertThat(r.message()).isEqualTo("done");
        assertThat(r.data()).isEqualTo("x");
    }

    @Test
    void created_setsMessage() {
        ApiResponse<Integer> r = ApiResponse.created("created", 1);
        assertThat(r.success()).isTrue();
        assertThat(r.message()).isEqualTo("created");
        assertThat(r.data()).isEqualTo(1);
    }
}

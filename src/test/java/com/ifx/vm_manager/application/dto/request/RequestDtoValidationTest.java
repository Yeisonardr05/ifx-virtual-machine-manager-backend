package com.ifx.vm_manager.application.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void createVmRequest_invalidValuesProduceViolations() {
        CreateVmRequest request = new CreateVmRequest("", 0, 0, 0, "");

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void updateVmRequest_invalidStatusProducesViolation() {
        UpdateVmRequest request = new UpdateVmRequest("ab", 1, 1, 1, "os", "INVALID");

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void loginRequest_invalidEmailProducesViolation() {
        LoginRequest request = new LoginRequest("not-an-email", "pw");

        assertThat(validator.validate(request)).isNotEmpty();
    }
}

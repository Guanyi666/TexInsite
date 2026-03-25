package com.texinsite.engine.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldRejectMissingEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("tester");
        request.setPassword("secret123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertTrue(
                violations.stream().anyMatch(violation -> "email".equals(violation.getPropertyPath().toString())),
                "email should be required"
        );
    }
}

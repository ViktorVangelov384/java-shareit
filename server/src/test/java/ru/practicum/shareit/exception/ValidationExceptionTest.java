package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ValidationExceptionTest {
    @Test
    void constructor_shouldSetMessage() {
        ValidationException ex = new ValidationException("Validation error");
        assertThat(ex.getMessage()).isEqualTo("Validation error");
    }
}
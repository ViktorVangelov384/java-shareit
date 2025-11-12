package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EmailAlreadyExistsExceptionTest {
    @Test
    void constructor_shouldSetMessage() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email exists");
        assertThat(ex.getMessage()).isEqualTo("Email exists");
    }
}

package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class NotFoundExceptionTest {
    @Test
    void constructor_shouldSetMessage() {
        String message = "Not found";
        NotFoundException exception = new NotFoundException(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }
}
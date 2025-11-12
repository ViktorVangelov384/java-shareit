package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AccessDeniedExceptionTest {
    @Test
    void constructor_shouldSetMessage() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        assertThat(ex.getMessage()).isEqualTo("Access denied");
    }
}

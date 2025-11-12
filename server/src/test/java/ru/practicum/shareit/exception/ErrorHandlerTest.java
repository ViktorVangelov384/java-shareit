package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleNotFoundException_shouldReturn404() {
        NotFoundException ex = new NotFoundException("Not found");
        ErrorResponse response = errorHandler.handleNotFoundException(ex);

        assertThat(response.getError()).isEqualTo("Not found");
    }

    @Test
    void handleIllegalArgumentException_shouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("Illegal argument");
        ErrorResponse response = errorHandler.handleIllegalArgumentException(ex);

        assertThat(response.getError()).isEqualTo("Illegal argument");
    }

    @Test
    void handleEmailAlreadyExistsException_shouldReturn409() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email exists");
        ErrorResponse response = errorHandler.handleEmailAlreadyExistsException(ex);

        assertThat(response.getError()).isEqualTo("Email exists");
    }

    @Test
    void handleConflictException_shouldReturn409() {
        ConflictException ex = new ConflictException("Conflict");
        ErrorResponse response = errorHandler.handleConflictException(ex);

        assertThat(response.getError()).isEqualTo("Conflict");
    }

    @Test
    void handleValidationException_shouldReturn400() {
        jakarta.validation.ValidationException ex = new jakarta.validation.ValidationException("Validation error");
        ErrorResponse response = errorHandler.handleValidationException(ex);

        assertThat(response.getError()).isEqualTo("Validation error");
    }

    @Test
    void handleAccessDeniedException_shouldReturn403() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        ErrorResponse response = errorHandler.handleAccessDeniedException(ex);

        assertThat(response.getError()).isEqualTo("Access denied");
    }

    @Test
    void handleDataIntegrityViolationException_shouldReturn409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Data integrity error");
        ErrorResponse response = errorHandler.handleDataIntegrityViolationException(ex);

        assertThat(response.getError()).contains("Data integrity error");
    }

    @Test
    void handleException_shouldReturn500() {
        Exception ex = new Exception("General error");
        ErrorResponse response = errorHandler.handleException(ex);

        assertThat(response.getError()).contains("General error");
    }
}

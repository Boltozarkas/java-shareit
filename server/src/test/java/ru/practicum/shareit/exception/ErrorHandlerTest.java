package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleNotFound_ShouldReturn404() {
        ResponseEntity<ErrorResponse> response = errorHandler.handleNotFound(
                new NotFoundException("User not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().getError());
    }

    @Test
    void handleForbidden_ShouldReturn403() {
        ResponseEntity<ErrorResponse> response = errorHandler.handleForbidden(
                new ForbiddenException("Access denied"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().getError());
    }

    @Test
    void handleDuplicateEmail_ShouldReturn409() {
        ResponseEntity<ErrorResponse> response = errorHandler.handleDuplicateEmail(
                new DuplicateEmailException("Email exists"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email exists", response.getBody().getError());
    }

    @Test
    void handleValidation_ShouldReturn400() {
        ResponseEntity<ErrorResponse> response = errorHandler.handleValidation(
                new IllegalArgumentException("Invalid argument"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid argument", response.getBody().getError());
    }

    @Test
    void handleOther_ShouldReturn500() {
        ResponseEntity<ErrorResponse> response = errorHandler.handleOther(
                new RuntimeException("Unexpected error"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().getError());
    }
}
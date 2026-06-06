package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorResponseTest {

    @Test
    void errorResponse_ShouldReturnError() {
        ErrorResponse errorResponse = new ErrorResponse("Test error");
        assertEquals("Test error", errorResponse.getError());
    }
}
package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingStateTest {

    @Test
    void from_ValidState_ShouldReturnState() {
        assertEquals(Optional.of(BookingState.ALL), BookingState.from("ALL"));
        assertEquals(Optional.of(BookingState.CURRENT), BookingState.from("CURRENT"));
        assertEquals(Optional.of(BookingState.PAST), BookingState.from("PAST"));
        assertEquals(Optional.of(BookingState.FUTURE), BookingState.from("FUTURE"));
        assertEquals(Optional.of(BookingState.WAITING), BookingState.from("WAITING"));
        assertEquals(Optional.of(BookingState.REJECTED), BookingState.from("REJECTED"));
    }

    @Test
    void from_InvalidState_ShouldReturnEmpty() {
        assertEquals(Optional.empty(), BookingState.from("INVALID"));
    }

    @Test
    void from_CaseInsensitive_ShouldWork() {
        assertEquals(Optional.of(BookingState.ALL), BookingState.from("all"));
        assertEquals(Optional.of(BookingState.CURRENT), BookingState.from("current"));
    }
}
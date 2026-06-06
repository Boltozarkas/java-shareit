package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    private Booking booking;
    private User booker;
    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        booker = new User();
        booker.setId(1L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        owner = new User();
        owner.setId(2L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
    }

    @Test
    void toBookingDto_ShouldMapAllFields() {
        BookingDto dto = BookingMapper.toBookingDto(booking);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(BookingStatus.WAITING, dto.getStatus());
        assertNotNull(dto.getBooker());
        assertEquals(1L, dto.getBooker().getId());
        assertEquals("Booker", dto.getBooker().getName());
        assertNotNull(dto.getItem());
        assertEquals(1L, dto.getItem().getId());
        assertEquals("Item", dto.getItem().getName());
    }

    @Test
    void toBookingDto_WithNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> BookingMapper.toBookingDto(null));
    }
}
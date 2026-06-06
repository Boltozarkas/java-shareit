package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;

    @BeforeEach
    void setUp() {
        owner = userService.create(new UserDto(null, "Owner", "owner@example.com"));
        booker = userService.create(new UserDto(null, "Booker", "booker@example.com"));
        item = itemService.create(owner.getId(), new ItemDto(null, "Item", "Description", true, null));
    }

    @Test
    void createBooking_ShouldReturnBookingDto() {
        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        BookingDto booking = bookingService.create(booker.getId(), createDto);

        assertNotNull(booking.getId());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }

    @Test
    void createBooking_WithOwnItem_ShouldThrowException() {
        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        assertThrows(NotFoundException.class, () -> bookingService.create(owner.getId(), createDto));
    }

    @Test
    void createBooking_WithUnavailableItem_ShouldThrowException() {
        itemService.update(owner.getId(), item.getId(),
                new ItemDto(item.getId(), "Item", "Description", false, null));

        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        assertThrows(IllegalArgumentException.class, () -> bookingService.create(booker.getId(), createDto));
    }

    @Test
    void updateStatus_ShouldApproveBooking() {
        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        BookingDto booking = bookingService.create(booker.getId(), createDto);

        BookingDto approved = bookingService.updateStatus(owner.getId(), booking.getId(), true);

        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    void updateStatus_ShouldRejectBooking() {
        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        BookingDto booking = bookingService.create(booker.getId(), createDto);

        BookingDto rejected = bookingService.updateStatus(owner.getId(), booking.getId(), false);

        assertEquals(BookingStatus.REJECTED, rejected.getStatus());
    }

    @Test
    void getByBooker_ShouldReturnBookingsByState() {
        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        bookingService.create(booker.getId(), createDto);

        List<BookingDto> bookings = bookingService.getByBooker(booker.getId(), BookingState.WAITING);
        assertEquals(1, bookings.size());
    }

    @Test
    void getByOwner_ShouldReturnBookingsByState() {
        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        bookingService.create(booker.getId(), createDto);

        List<BookingDto> bookings = bookingService.getByOwner(owner.getId(), BookingState.ALL);
        assertEquals(1, bookings.size());
    }
}
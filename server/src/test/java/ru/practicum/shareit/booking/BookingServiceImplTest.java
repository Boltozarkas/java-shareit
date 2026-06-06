package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

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
    void create_ShouldReturnBookingDto() {
        BookingCreateDto createDto = new BookingCreateDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        when(userRepository.findById(eq(2L))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.create(2L, createDto);

        assertNotNull(result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void create_WithNonExistentUser_ShouldThrowException() {
        BookingCreateDto createDto = new BookingCreateDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        when(userRepository.findById(eq(999L))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(999L, createDto));
    }

    @Test
    void create_WithNonExistentItem_ShouldThrowException() {
        BookingCreateDto createDto = new BookingCreateDto(
                999L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        when(userRepository.findById(eq(2L))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(eq(999L))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(2L, createDto));
    }

    @Test
    void create_WithOwnItem_ShouldThrowException() {
        BookingCreateDto createDto = new BookingCreateDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(owner));
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.create(1L, createDto));
    }

    @Test
    void create_WithUnavailableItem_ShouldThrowException() {
        item.setAvailable(false);
        BookingCreateDto createDto = new BookingCreateDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        when(userRepository.findById(eq(2L))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> bookingService.create(2L, createDto));
    }

    @Test
    void create_WithStartAfterEnd_ShouldThrowException() {
        BookingCreateDto createDto = new BookingCreateDto(
                1L, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1));

        when(userRepository.findById(eq(2L))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> bookingService.create(2L, createDto));
    }

    @Test
    void create_WithNullDates_ShouldThrowException() {
        BookingCreateDto createDto = new BookingCreateDto(1L, null, null);

        when(userRepository.findById(eq(2L))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> bookingService.create(2L, createDto));
    }

    @Test
    void updateStatus_ShouldApproveBooking() {
        when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.updateStatus(1L, 1L, true);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void updateStatus_ShouldRejectBooking() {
        when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.updateStatus(1L, 1L, false);

        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    void updateStatus_ByNonOwner_ShouldThrowException() {
        when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenException.class, () -> bookingService.updateStatus(2L, 1L, true));
    }

    @Test
    void updateStatus_AlreadyProcessed_ShouldThrowException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));

        assertThrows(IllegalArgumentException.class, () -> bookingService.updateStatus(1L, 1L, true));
    }

    @Test
    void updateStatus_NonExistentBooking_ShouldThrowException() {
        when(bookingRepository.findById(eq(999L))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.updateStatus(1L, 999L, true));
    }

    @Test
    void getById_ByBooker_ShouldReturnBooking() {
        when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getById(2L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_ByOwner_ShouldReturnBooking() {
        when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getById(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void getById_ByStranger_ShouldThrowException() {
        when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getById(3L, 1L));
    }

    @Test
    void getById_NonExistent_ShouldThrowException() {
        when(bookingRepository.findById(eq(999L))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getById(1L, 999L));
    }

    @Test
    void getByBooker_AllState_ShouldReturnBookings() {
        when(userRepository.existsById(eq(2L))).thenReturn(true);
        when(bookingRepository.findByBookerId(eq(2L), any(Sort.class))).thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getByBooker(2L, BookingState.ALL);

        assertEquals(1, result.size());
    }

    @Test
    void getByBooker_CurrentState_ShouldReturnBookings() {
        when(userRepository.existsById(eq(2L))).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                eq(2L), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getByBooker(2L, BookingState.CURRENT);

        assertEquals(1, result.size());
    }

    @Test
    void getByBooker_PastState_ShouldReturnBookings() {
        when(userRepository.existsById(eq(2L))).thenReturn(true);
        when(bookingRepository.findByBookerIdAndEndBefore(eq(2L), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getByBooker(2L, BookingState.PAST);

        assertEquals(1, result.size());
    }

    @Test
    void getByBooker_FutureState_ShouldReturnBookings() {
        when(userRepository.existsById(eq(2L))).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStartAfter(eq(2L), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getByBooker(2L, BookingState.FUTURE);

        assertEquals(1, result.size());
    }

    @Test
    void getByBooker_WaitingState_ShouldReturnBookings() {
        when(userRepository.existsById(eq(2L))).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStatus(eq(2L), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getByBooker(2L, BookingState.WAITING);

        assertEquals(1, result.size());
    }

    @Test
    void getByBooker_RejectedState_ShouldReturnBookings() {
        when(userRepository.existsById(eq(2L))).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStatus(eq(2L), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getByBooker(2L, BookingState.REJECTED);

        assertEquals(1, result.size());
    }

    @Test
    void getByBooker_NonExistentUser_ShouldThrowException() {
        when(userRepository.existsById(eq(999L))).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.getByBooker(999L, BookingState.ALL));
    }

    @Test
    void getByOwner_AllState_ShouldReturnBookings() {
        when(userRepository.existsById(eq(1L))).thenReturn(true);
        when(bookingRepository.findByItemOwnerId(eq(1L), any(Sort.class))).thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getByOwner(1L, BookingState.ALL);

        assertEquals(1, result.size());
    }

    @Test
    void getByOwner_CurrentState_ShouldReturnBookings() {
        when(userRepository.existsById(eq(1L))).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getByOwner(1L, BookingState.CURRENT);

        assertEquals(1, result.size());
    }

    @Test
    void getByOwner_PastState_ShouldReturnBookings() {
        when(userRepository.existsById(eq(1L))).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdAndEndBefore(eq(1L), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getByOwner(1L, BookingState.PAST);

        assertEquals(1, result.size());
    }

    @Test
    void getByOwner_FutureState_ShouldReturnBookings() {
        when(userRepository.existsById(eq(1L))).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdAndStartAfter(eq(1L), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getByOwner(1L, BookingState.FUTURE);

        assertEquals(1, result.size());
    }

    @Test
    void getByOwner_WaitingState_ShouldReturnBookings() {
        when(userRepository.existsById(eq(1L))).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdAndStatus(eq(1L), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getByOwner(1L, BookingState.WAITING);

        assertEquals(1, result.size());
    }

    @Test
    void getByOwner_RejectedState_ShouldReturnBookings() {
        when(userRepository.existsById(eq(1L))).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdAndStatus(eq(1L), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getByOwner(1L, BookingState.REJECTED);

        assertEquals(1, result.size());
    }

    @Test
    void getByOwner_NonExistentUser_ShouldThrowException() {
        when(userRepository.existsById(eq(999L))).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.getByOwner(999L, BookingState.ALL));
    }
}
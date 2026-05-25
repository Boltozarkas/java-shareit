package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingCreateDto dto) {
        User booker = getUserById(userId);
        Item item = getItemById(dto.getItemId());

        validateBookingCreation(booker, item, dto);

        Booking booking = buildBooking(dto, item, booker);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto updateStatus(Long userId, Long bookingId, boolean approved) {
        Booking booking = getBookingById(bookingId);

        validateBookingOwnership(booking, userId);
        validateBookingStatus(booking);

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = getBookingById(bookingId);
        validateBookingAccess(booking, userId);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(Long userId, BookingState state) {
        validateUserExists(userId);

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = findBookingsByBookerAndState(userId, state, sort, now);

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getByOwner(Long userId, BookingState state) {
        validateUserExists(userId);

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = findBookingsByOwnerAndState(userId, state, sort, now);

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    // ===== Методы получения сущностей =====

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
    }

    private Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
    }

    // ===== Методы валидации =====

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
    }

    private void validateBookingCreation(User booker, Item item, BookingCreateDto dto) {
        validateItemAvailable(item);
        validateNotOwnerBooking(booker, item);
        validateBookingDates(dto);
    }

    private void validateItemAvailable(Item item) {
        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Item is not available for booking");
        }
    }

    private void validateNotOwnerBooking(User booker, Item item) {
        Long ownerId = Optional.ofNullable(item.getOwner())
                .map(User::getId)
                .orElseThrow(() -> new NotFoundException("Item has no owner"));

        if (ownerId.equals(booker.getId())) {
            throw new NotFoundException("You cannot book your own item");
        }
    }

    private void validateBookingDates(BookingCreateDto dto) {
        if (dto.getStart() == null) {
            throw new IllegalArgumentException("Start date must not be null");
        }
        if (dto.getEnd() == null) {
            throw new IllegalArgumentException("End date must not be null");
        }
        if (!dto.getStart().isBefore(dto.getEnd())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private void validateBookingOwnership(Booking booking, Long userId) {
        Long ownerId = Optional.ofNullable(booking)
                .map(Booking::getItem)
                .map(Item::getOwner)
                .map(User::getId)
                .orElseThrow(() -> new NotFoundException("Booking not found or has no owner"));

        if (!ownerId.equals(userId)) {
            throw new ForbiddenException("Only the owner can approve/reject the booking");
        }
    }

    private void validateBookingStatus(Booking booking) {
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Booking has already been processed");
        }
    }

    private void validateBookingAccess(Booking booking, Long userId) {
        Long bookerId = Optional.ofNullable(booking.getBooker())
                .map(User::getId)
                .orElseThrow(() -> new NotFoundException("Booking has no booker"));

        Long ownerId = Optional.ofNullable(booking.getItem())
                .map(Item::getOwner)
                .map(User::getId)
                .orElseThrow(() -> new NotFoundException("Booking has no owner"));

        boolean isBooker = bookerId.equals(userId);
        boolean isOwner = ownerId.equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Booking not found: " + booking.getId());
        }
    }

    // ===== Методы запросов к репозиторию =====

    private List<Booking> findBookingsByBookerAndState(Long userId, BookingState state, Sort sort, LocalDateTime now) {
        return switch (state) {
            case ALL -> bookingRepository.findByBookerId(userId, sort);
            case CURRENT -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(userId, now, now, sort);
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(userId, now, sort);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(userId, now, sort);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, sort);
        };
    }

    private List<Booking> findBookingsByOwnerAndState(Long userId, BookingState state, Sort sort, LocalDateTime now) {
        return switch (state) {
            case ALL -> bookingRepository.findByItemOwnerId(userId, sort);
            case CURRENT -> bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(userId, now, now, sort);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBefore(userId, now, sort);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfter(userId, now, sort);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, sort);
        };
    }

    // ===== Методы построения объектов =====

    private Booking buildBooking(BookingCreateDto dto, Item item, User booker) {
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }
}
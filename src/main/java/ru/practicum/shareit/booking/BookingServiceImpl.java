package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
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
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found: " + dto.getItemId()));

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Item is not available for booking");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("You cannot book your own item");
        }

        if (!dto.getStart().isBefore(dto.getEnd())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto updateStatus(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            // Возвращаем 403 вместо 404 для случаев, когда пользователь не владелец
            throw new IllegalArgumentException("Only the owner can approve/reject the booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Booking has already been processed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Booking not found: " + bookingId);
        }

        return toDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBookerId(userId, sort);
            case CURRENT -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(userId, now, now, sort);
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(userId, now, sort);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(userId, now, sort);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, sort);
        };

        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getByOwner(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItemOwnerId(userId, sort);
            case CURRENT -> bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(userId, now, now, sort);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBefore(userId, now, sort);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfter(userId, now, sort);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, sort);
        };

        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private BookingDto toDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());

        // Создаём вложенный объект booker
        User booker = booking.getBooker();
        UserDto bookerDto = new UserDto();
        bookerDto.setId(booker.getId());
        bookerDto.setName(booker.getName());
        bookerDto.setEmail(booker.getEmail());
        dto.setBooker(bookerDto);

        // Создаём вложенный объект item
        Item item = booking.getItem();
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        dto.setItem(itemDto);

        return dto;
    }
}
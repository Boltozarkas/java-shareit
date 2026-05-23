package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.BookingInfo;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = getUserById(userId);
        validateItemDto(itemDto);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = getItemById(itemId);
        validateItemOwnership(item, userId);

        updateItemFields(item, itemDto);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemResponseDto getById(Long itemId, Long userId) {
        Item item = getItemById(itemId);
        boolean isOwner = isUserOwnerOfItem(item, userId);

        List<CommentDto> comments = getCommentsForItem(itemId);
        BookingInfo lastBooking = isOwner ? getLastBookingForItem(itemId) : null;
        BookingInfo nextBooking = isOwner ? getNextBookingForItem(itemId) : null;

        return ItemMapper.toItemResponseDto(item, comments, lastBooking, nextBooking);
    }

    @Override
    public List<ItemResponseDto> getByOwnerId(Long userId) {
        validateUserExists(userId);

        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        // Собираем ID всех вещей
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        // Загружаем все комментарии одним запросом
        Map<Long, List<CommentDto>> commentsByItemId = getCommentsByItemIds(itemIds);

        // Загружаем все бронирования одним запросом
        LocalDateTime now = LocalDateTime.now();
        Map<Long, BookingInfo> lastBookings = getLastBookingsForItems(itemIds, now);
        Map<Long, BookingInfo> nextBookings = getNextBookingsForItems(itemIds, now);

        // Собираем DTO без дополнительных запросов к БД
        return items.stream()
                .map(item -> {
                    List<CommentDto> comments = commentsByItemId.getOrDefault(item.getId(), Collections.emptyList());
                    BookingInfo lastBooking = lastBookings.get(item.getId());
                    BookingInfo nextBooking = nextBookings.get(item.getId());
                    return ItemMapper.toItemResponseDto(item, comments, lastBooking, nextBooking);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, String text) {
        User author = getUserById(userId);
        Item item = getItemById(itemId);

        validateCommentCreation(userId, itemId, text);

        Comment comment = buildComment(text, item, author);
        Comment saved = commentRepository.save(comment);
        return ItemMapper.toCommentDto(saved);
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

    // ===== Методы валидации =====

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
    }

    private void validateItemDto(ItemDto itemDto) {
        Optional.ofNullable(itemDto.getName())
                .filter(String::isBlank)
                .ifPresent(n -> {
                    throw new IllegalArgumentException("Name cannot be blank");
                });

        Optional.ofNullable(itemDto.getDescription())
                .filter(String::isBlank)
                .ifPresent(d -> {
                    throw new IllegalArgumentException("Description cannot be blank");
                });

        if (itemDto.getAvailable() == null) {
            throw new IllegalArgumentException("Available cannot be null");
        }
    }

    private void validateItemOwnership(Item item, Long userId) {
        Long ownerId = Optional.ofNullable(item)
                .map(Item::getOwner)
                .map(User::getId)
                .orElseThrow(() -> new NotFoundException("Item not found or has no owner"));

        if (!ownerId.equals(userId)) {
            throw new ForbiddenException("You are not the owner of this item");
        }
    }

    private void validateCommentCreation(Long userId, Long itemId, String text) {
        validateCommentText(text);
        validateUserHasCompletedBooking(userId, itemId);
    }

    private void validateCommentText(String text) {
        Optional.ofNullable(text)
                .filter(String::isBlank)
                .ifPresent(t -> {
                    throw new IllegalArgumentException("Comment text cannot be blank");
                });
    }

    private void validateUserHasCompletedBooking(Long userId, Long itemId) {
        List<Booking> completedBookings = bookingRepository
                .findCompletedBookingsByItemAndUser(itemId, userId, LocalDateTime.now());

        if (completedBookings.isEmpty()) {
            throw new IllegalArgumentException("You can only comment on items you have rented and returned");
        }
    }

    // ===== Методы обновления полей =====

    private void updateItemFields(Item item, ItemDto itemDto) {
        Optional.ofNullable(itemDto.getName())
                .filter(name -> !name.isBlank())
                .ifPresent(item::setName);

        Optional.ofNullable(itemDto.getDescription())
                .filter(desc -> !desc.isBlank())
                .ifPresent(item::setDescription);

        Optional.ofNullable(itemDto.getAvailable())
                .ifPresent(item::setAvailable);
    }

    // ===== Методы построения объектов =====

    private Comment buildComment(String text, Item item, User author) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    // ===== Методы проверки прав =====

    private boolean isUserOwnerOfItem(Item item, Long userId) {
        return Optional.ofNullable(item.getOwner())
                .map(User::getId)
                .filter(id -> id.equals(userId))
                .isPresent();
    }

    // ===== Методы загрузки данных (одиночные) =====

    private List<CommentDto> getCommentsForItem(Long itemId) {
        List<Comment> comments = commentRepository.findByItemId(itemId);
        return ItemMapper.toCommentDtoList(comments);
    }

    private BookingInfo getLastBookingForItem(Long itemId) {
        return bookingRepository
                .findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
                        itemId, LocalDateTime.now(), BookingStatus.APPROVED)
                .map(ItemMapper::toBookingInfo)
                .orElse(null);
    }

    private BookingInfo getNextBookingForItem(Long itemId) {
        return bookingRepository
                .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                        itemId, LocalDateTime.now(), BookingStatus.APPROVED)
                .map(ItemMapper::toBookingInfo)
                .orElse(null);
    }

    // ===== Методы пакетной загрузки данных =====

    private Map<Long, List<CommentDto>> getCommentsByItemIds(List<Long> itemIds) {
        List<Comment> comments = commentRepository.findByItemIdIn(itemIds);
        return ItemMapper.groupCommentsByItemId(comments);
    }

    private Map<Long, BookingInfo> getLastBookingsForItems(List<Long> itemIds, LocalDateTime now) {
        List<Booking> bookings = bookingRepository
                .findByItemIdInAndStatus(itemIds, BookingStatus.APPROVED);
        return ItemMapper.findLastBookings(bookings, now);
    }

    private Map<Long, BookingInfo> getNextBookingsForItems(List<Long> itemIds, LocalDateTime now) {
        List<Booking> bookings = bookingRepository
                .findByItemIdInAndStatus(itemIds, BookingStatus.APPROVED);
        return ItemMapper.findNextBookings(bookings, now);
    }
}
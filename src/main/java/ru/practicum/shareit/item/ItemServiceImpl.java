package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
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
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Item not found: " + itemId);
        }

        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                throw new IllegalArgumentException("Description cannot be blank");
            }
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemResponseDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
        return mapToResponseDto(item, userId);
    }

    @Override
    public List<ItemResponseDto> getByOwnerId(Long userId) {
        return itemRepository.findByOwnerId(userId).stream()
                .map(item -> mapToResponseDto(item, userId))
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
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        // Проверяем, что пользователь брал вещь в аренду и аренда завершена
        List<Booking> completedBookings = bookingRepository
                .findCompletedBookingsByItemAndUser(itemId, userId, LocalDateTime.now());

        if (completedBookings.isEmpty()) {
            throw new IllegalArgumentException("You can only comment on items you have rented and returned");
        }

        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        return toCommentDto(saved);
    }

    private ItemResponseDto mapToResponseDto(Item item, Long userId) {
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setRequestId(item.getRequestId());

        // Загружаем комментарии
        List<Comment> comments = commentRepository.findByItemId(item.getId());
        dto.setComments(comments.stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList()));

        // Добавляем информацию о бронированиях только для владельца
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            Optional<Booking> lastBooking = bookingRepository
                    .findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
                            item.getId(), now, BookingStatus.APPROVED);
            Optional<Booking> nextBooking = bookingRepository
                    .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                            item.getId(), now, BookingStatus.APPROVED);

            dto.setLastBooking(lastBooking.map(b -> new BookingInfo(
                    b.getId(), b.getBooker().getId(), b.getStart(), b.getEnd())).orElse(null));
            dto.setNextBooking(nextBooking.map(b -> new BookingInfo(
                    b.getId(), b.getBooker().getId(), b.getStart(), b.getEnd())).orElse(null));
        } else {
            // Для не владельцев явно устанавливаем null
            dto.setLastBooking(null);
            dto.setNextBooking(null);
        }

        return dto;
    }

    private CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setCreated(comment.getCreated());
        return dto;
    }
}
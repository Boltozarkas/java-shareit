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

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You are not the owner of this item");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
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

        List<CommentDto> comments = ItemMapper.toCommentDtoList(commentRepository.findByItemId(itemId));

        BookingInfo lastBooking = null;
        BookingInfo nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            lastBooking = bookingRepository
                    .findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(itemId, now, BookingStatus.APPROVED)
                    .map(ItemMapper::toBookingInfo)
                    .orElse(null);
            nextBooking = bookingRepository
                    .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(itemId, now, BookingStatus.APPROVED)
                    .map(ItemMapper::toBookingInfo)
                    .orElse(null);
        }

        return ItemMapper.toItemResponseDto(item, comments, lastBooking, nextBooking);
    }

    @Override
    public List<ItemResponseDto> getByOwnerId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }

        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) return Collections.emptyList();

        List<Long> itemIds = items.stream().map(Item::getId).toList();
        Map<Long, List<CommentDto>> commentsByItemId = ItemMapper.groupCommentsByItemId(
                commentRepository.findByItemIdIn(itemIds));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> approvedBookings = bookingRepository.findByItemIdInAndStatus(itemIds, BookingStatus.APPROVED);
        Map<Long, BookingInfo> lastBookings = ItemMapper.findLastBookings(approvedBookings, now);
        Map<Long, BookingInfo> nextBookings = ItemMapper.findNextBookings(approvedBookings, now);

        return items.stream()
                .map(item -> ItemMapper.toItemResponseDto(
                        item,
                        commentsByItemId.getOrDefault(item.getId(), Collections.emptyList()),
                        lastBookings.get(item.getId()),
                        nextBookings.get(item.getId())
                ))
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, String text) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

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

        return ItemMapper.toCommentDto(commentRepository.save(comment));
    }
}
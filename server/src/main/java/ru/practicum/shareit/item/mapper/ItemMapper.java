package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.BookingInfo;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class ItemMapper {

    public ItemDto toItemDto(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setRequestId(item.getRequestId());
        return dto;
    }

    public Item toItem(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setRequestId(itemDto.getRequestId());
        return item;
    }

    public ItemResponseDto toItemResponseDto(Item item,
                                             List<CommentDto> comments,
                                             BookingInfo lastBooking,
                                             BookingInfo nextBooking) {
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setRequestId(item.getRequestId());
        dto.setComments(comments != null ? comments : Collections.emptyList());
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        return dto;
    }

    public CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Comment cannot be null");
        }
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthorName(Optional.ofNullable(comment.getAuthor()).map(User::getName).orElse("Unknown"));
        dto.setCreated(comment.getCreated());
        return dto;
    }

    public BookingInfo toBookingInfo(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new BookingInfo(
                booking.getId(),
                booking.getBooker().getId(),
                booking.getStart(),
                booking.getEnd()
        );
    }

    public List<CommentDto> toCommentDtoList(List<Comment> comments) {
        if (comments == null) {
            return Collections.emptyList();
        }
        return comments.stream()
                .map(ItemMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public Map<Long, List<CommentDto>> groupCommentsByItemId(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return Collections.emptyMap();
        }
        return comments.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(ItemMapper::toCommentDto, Collectors.toList())
                ));
    }

    public Map<Long, BookingInfo> findLastBookings(List<Booking> bookings, LocalDateTime now) {
        if (bookings == null || bookings.isEmpty()) {
            return Collections.emptyMap();
        }
        return bookings.stream()
                .filter(b -> b.getEnd().isBefore(now))
                .collect(Collectors.groupingBy(
                        b -> b.getItem().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(Booking::getEnd)),
                                opt -> opt.map(ItemMapper::toBookingInfo).orElse(null)
                        )
                ));
    }

    public Map<Long, BookingInfo> findNextBookings(List<Booking> bookings, LocalDateTime now) {
        if (bookings == null || bookings.isEmpty()) {
            return Collections.emptyMap();
        }
        return bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .collect(Collectors.groupingBy(
                        b -> b.getItem().getId(),
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(Booking::getStart)),
                                opt -> opt.map(ItemMapper::toBookingInfo).orElse(null)
                        )
                ));
    }
}
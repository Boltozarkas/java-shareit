package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.BookingInfo;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    private Item item;
    private Comment comment;
    private Booking booking;
    private User author;
    private User booker;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(1L);
        author.setName("Author");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");

        item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setRequestId(1L);

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        comment.setItem(item);

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);
    }

    @Test
    void toItemDto_ShouldMapAllFields() {
        ItemDto dto = ItemMapper.toItemDto(item);

        assertEquals(1L, dto.getId());
        assertEquals("Item", dto.getName());
        assertEquals("Description", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(1L, dto.getRequestId());
    }

    @Test
    void toItemDto_WithNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> ItemMapper.toItemDto(null));
    }

    @Test
    void toCommentDto_ShouldMapAllFields() {
        CommentDto dto = ItemMapper.toCommentDto(comment);

        assertEquals(1L, dto.getId());
        assertEquals("Great item!", dto.getText());
        assertEquals("Author", dto.getAuthorName());
        assertNotNull(dto.getCreated());
    }

    @Test
    void toCommentDto_WithNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> ItemMapper.toCommentDto(null));
    }

    @Test
    void toBookingInfo_ShouldMapAllFields() {
        BookingInfo info = ItemMapper.toBookingInfo(booking);

        assertNotNull(info);
        assertEquals(1L, info.getId());
        assertEquals(2L, info.getBookerId());
        assertNotNull(info.getStart());
        assertNotNull(info.getEnd());
    }

    @Test
    void toBookingInfo_WithNull_ShouldReturnNull() {
        BookingInfo info = ItemMapper.toBookingInfo(null);
        assertNull(info);
    }

    @Test
    void toItemResponseDto_ShouldMapAllFields() {
        CommentDto commentDto = ItemMapper.toCommentDto(comment);
        BookingInfo bookingInfo = ItemMapper.toBookingInfo(booking);

        ItemResponseDto dto = ItemMapper.toItemResponseDto(item, List.of(commentDto), bookingInfo, null);

        assertEquals(1L, dto.getId());
        assertEquals("Item", dto.getName());
        assertEquals(1, dto.getComments().size());
        assertNotNull(dto.getLastBooking());
        assertNull(dto.getNextBooking());
    }

    @Test
    void groupCommentsByItemId_ShouldGroupComments() {
        Map<Long, List<CommentDto>> result = ItemMapper.groupCommentsByItemId(List.of(comment));

        assertEquals(1, result.size());
        assertTrue(result.containsKey(1L));
        assertEquals(1, result.get(1L).size());
    }

    @Test
    void groupCommentsByItemId_WithEmptyList_ShouldReturnEmptyMap() {
        Map<Long, List<CommentDto>> result = ItemMapper.groupCommentsByItemId(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void findLastBookings_ShouldFindLastBooking() {
        LocalDateTime now = LocalDateTime.now().plusDays(3);
        Map<Long, BookingInfo> result = ItemMapper.findLastBookings(List.of(booking), now);

        assertEquals(1, result.size());
        assertNotNull(result.get(1L));
    }

    @Test
    void findNextBookings_ShouldFindNextBooking() {
        LocalDateTime now = LocalDateTime.now();
        Map<Long, BookingInfo> result = ItemMapper.findNextBookings(List.of(booking), now);

        assertEquals(1, result.size());
        assertNotNull(result.get(1L));
    }
}
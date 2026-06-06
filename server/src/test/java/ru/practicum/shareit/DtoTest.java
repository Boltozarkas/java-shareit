package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void userDto_ShouldWork() {
        UserDto dto = new UserDto(1L, "John", "john@example.com");

        assertEquals(1L, dto.getId());
        assertEquals("John", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
    }

    @Test
    void userDto_AllArgsConstructor_ShouldWork() {
        UserDto dto = new UserDto(1L, "John", "john@example.com");
        assertNotNull(dto);
    }

    @Test
    void userDto_NoArgsConstructor_ShouldWork() {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("John");
        dto.setEmail("john@example.com");

        assertEquals(1L, dto.getId());
    }

    @Test
    void itemDto_ShouldWork() {
        ItemDto dto = new ItemDto(1L, "Item", "Description", true, 1L);

        assertEquals(1L, dto.getId());
        assertEquals("Item", dto.getName());
        assertTrue(dto.getAvailable());
        assertEquals(1L, dto.getRequestId());
    }

    @Test
    void bookingCreateDto_ShouldWork() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingCreateDto dto = new BookingCreateDto(1L, start, end);

        assertEquals(1L, dto.getItemId());
        assertEquals(start, dto.getStart());
        assertEquals(end, dto.getEnd());
    }

    @Test
    void bookingDto_ShouldWork() {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStatus(BookingStatus.WAITING);

        assertEquals(1L, dto.getId());
        assertEquals(BookingStatus.WAITING, dto.getStatus());
    }

    @Test
    void commentDto_ShouldWork() {
        LocalDateTime now = LocalDateTime.now();
        CommentDto dto = new CommentDto(1L, "Great!", "Author", now);

        assertEquals(1L, dto.getId());
        assertEquals("Great!", dto.getText());
        assertEquals("Author", dto.getAuthorName());
        assertEquals(now, dto.getCreated());
    }

    @Test
    void commentCreateDto_ShouldWork() {
        CommentCreateDto dto = new CommentCreateDto("Great item!");
        assertEquals("Great item!", dto.getText());
    }

    @Test
    void bookingInfo_ShouldWork() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingInfo info = new BookingInfo(1L, 2L, start, end);

        assertEquals(1L, info.getId());
        assertEquals(2L, info.getBookerId());
        assertEquals(start, info.getStart());
        assertEquals(end, info.getEnd());
    }

    @Test
    void itemResponseDto_ShouldWork() {
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(1L);
        dto.setName("Item");
        dto.setComments(List.of());

        assertEquals(1L, dto.getId());
        assertEquals("Item", dto.getName());
        assertNotNull(dto.getComments());
        assertTrue(dto.getComments().isEmpty());
    }

    @Test
    void itemRequestCreateDto_ShouldWork() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Need something");
        assertEquals("Need something", dto.getDescription());
    }

    @Test
    void itemRequestDto_ShouldWork() {
        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto dto = new ItemRequestDto(1L, "Need something", now, List.of());

        assertEquals(1L, dto.getId());
        assertEquals("Need something", dto.getDescription());
        assertEquals(now, dto.getCreated());
        assertTrue(dto.getItems().isEmpty());
    }
}
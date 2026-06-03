package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

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
    void createItem_ShouldReturnItemDto() {
        ItemDto created = itemService.create(owner.getId(),
                new ItemDto(null, "New Item", "New Description", true, null));

        assertNotNull(created.getId());
        assertEquals("New Item", created.getName());
    }

    @Test
    void updateItem_ShouldUpdateFields() {
        ItemDto updated = itemService.update(owner.getId(), item.getId(),
                new ItemDto(null, "Updated Item", "Updated Description", false, null));

        assertEquals("Updated Item", updated.getName());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals(false, updated.getAvailable());
    }

    @Test
    void getById_ShouldReturnItemWithBookings() {
        // Создаем бронирование
        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
        );
        BookingDto booking = bookingService.create(booker.getId(), createDto);
        bookingService.updateStatus(owner.getId(), booking.getId(), true);

        ItemResponseDto found = itemService.getById(item.getId(), owner.getId());

        assertNotNull(found.getLastBooking());
        assertNull(found.getNextBooking());
    }

    @Test
    void search_ShouldFindItems() {
        List<ItemDto> found = itemService.search("Description");
        assertEquals(1, found.size());
    }

    @Test
    void search_WithEmptyText_ShouldReturnEmptyList() {
        List<ItemDto> found = itemService.search("");
        assertTrue(found.isEmpty());
    }

    @Test
    void addComment_ShouldReturnCommentDto() {
        // Создаем завершенное бронирование
        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
        );
        BookingDto booking = bookingService.create(booker.getId(), createDto);
        bookingService.updateStatus(owner.getId(), booking.getId(), true);

        CommentDto comment = itemService.addComment(booker.getId(), item.getId(), "Great item!");

        assertNotNull(comment.getId());
        assertEquals("Great item!", comment.getText());
        assertEquals("Booker", comment.getAuthorName());
    }

    @Test
    void addComment_WithoutBooking_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> itemService.addComment(booker.getId(), item.getId(), "Great item!"));
    }
}
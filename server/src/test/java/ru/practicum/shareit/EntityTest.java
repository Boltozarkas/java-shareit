package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void userEntity_ShouldWork() {
        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@example.com");

        assertEquals(1L, user.getId());
        assertEquals("John", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertNotNull(user.toString());
    }

    @Test
    void itemEntity_ShouldWork() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequestId(1L);

        assertEquals(1L, item.getId());
        assertEquals("Item", item.getName());
        assertTrue(item.getAvailable());
        assertEquals(owner, item.getOwner());
        assertEquals(1L, item.getRequestId());
        assertNotNull(item.toString());
    }

    @Test
    void bookingEntity_ShouldWork() {
        User booker = new User();
        booker.setId(1L);
        Item item = new Item();
        item.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now());
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        assertEquals(1L, booking.getId());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
        assertNotNull(booking.toString());
    }

    @Test
    void commentEntity_ShouldWork() {
        User author = new User();
        author.setId(1L);
        Item item = new Item();
        item.setId(1L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great!");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        assertEquals(1L, comment.getId());
        assertEquals("Great!", comment.getText());
        assertEquals(author, comment.getAuthor());
        assertEquals(item, comment.getItem());
        assertNotNull(comment.getCreated());
        assertNotNull(comment.toString());
    }

    @Test
    void itemRequestEntity_ShouldWork() {
        User requestor = new User();
        requestor.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need something");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        assertEquals(1L, request.getId());
        assertEquals("Need something", request.getDescription());
        assertEquals(requestor, request.getRequestor());
        assertNotNull(request.getCreated());
    }
}
package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private Booking lastBooking;
    private Booking nextBooking;
    private Comment comment;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);

        lastBooking = new Booking();
        lastBooking.setId(1L);
        lastBooking.setStart(LocalDateTime.now().minusDays(5));
        lastBooking.setEnd(LocalDateTime.now().minusDays(3));
        lastBooking.setItem(item);
        lastBooking.setBooker(booker);
        lastBooking.setStatus(BookingStatus.APPROVED);

        nextBooking = new Booking();
        nextBooking.setId(2L);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(3));
        nextBooking.setItem(item);
        nextBooking.setBooker(booker);
        nextBooking.setStatus(BookingStatus.APPROVED);

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setCreated(LocalDateTime.now());
    }

    @Test
    void create_ShouldReturnItemDto() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.create(1L, new ItemDto(null, "Item", "Description", true, null));

        assertNotNull(result.getId());
        assertEquals("Item", result.getName());
    }

    @Test
    void create_WithRequestId_ShouldReturnItemDto() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.create(1L, new ItemDto(null, "Item", "Description", true, 1L));

        assertNotNull(result.getId());
        assertEquals("Item", result.getName());
    }

    @Test
    void create_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.findById(eq(999L))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.create(999L, new ItemDto(null, "Item", "Description", true, null)));
    }

    @Test
    void update_ShouldReturnUpdatedItem() {
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto updateDto = new ItemDto(null, "Updated", "Updated Description", false, null);
        ItemDto result = itemService.update(1L, 1L, updateDto);

        assertEquals("Updated", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(false, result.getAvailable());
    }

    @Test
    void update_OnlyName_ShouldUpdateName() {
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");
        ItemDto result = itemService.update(1L, 1L, updateDto);

        assertEquals("Updated Name", result.getName());
        assertEquals("Description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void update_OnlyDescription_ShouldUpdateDescription() {
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto updateDto = new ItemDto();
        updateDto.setDescription("Updated Description");
        ItemDto result = itemService.update(1L, 1L, updateDto);

        assertEquals("Item", result.getName());
        assertEquals("Updated Description", result.getDescription());
    }

    @Test
    void update_OnlyAvailable_ShouldUpdateAvailable() {
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto updateDto = new ItemDto();
        updateDto.setAvailable(false);
        ItemDto result = itemService.update(1L, 1L, updateDto);

        assertEquals("Item", result.getName());
        assertFalse(result.getAvailable());
    }

    @Test
    void update_WithBlankName_ShouldNotUpdate() {
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto updateDto = new ItemDto();
        updateDto.setName("   ");  // blank строка (только пробелы)
        ItemDto result = itemService.update(1L, 1L, updateDto);

        assertEquals("Item", result.getName());  // Имя не должно измениться
    }

    @Test
    void update_ByNonOwner_ShouldThrowException() {
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class,
                () -> itemService.update(2L, 1L, new ItemDto(null, "Updated", "Desc", true, null)));
    }

    @Test
    void update_NonExistentItem_ShouldThrowException() {
        when(itemRepository.findById(eq(999L))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.update(1L, 999L, new ItemDto(null, "Updated", "Desc", true, null)));
    }

    @Test
    void getById_ShouldReturnItemResponseDto() {
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(eq(1L))).thenReturn(Collections.emptyList());

        ItemResponseDto result = itemService.getById(1L, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Item", result.getName());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void getById_ByOwner_ShouldReturnItemWithBookings() {
        LocalDateTime now = LocalDateTime.now();
        lastBooking.setEnd(now.minusDays(1));
        nextBooking.setStart(now.plusDays(1));

        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(eq(1L))).thenReturn(Collections.emptyList());
        when(bookingRepository.findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
                eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.of(nextBooking));

        ItemResponseDto result = itemService.getById(1L, 1L);

        assertNotNull(result);
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
    }

    @Test
    void getById_NonExistentItem_ShouldThrowException() {
        when(itemRepository.findById(eq(999L))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getById(999L, 1L));
    }

    @Test
    void getByOwnerId_ShouldReturnItems() {
        when(userRepository.existsById(eq(1L))).thenReturn(true);
        when(itemRepository.findByOwnerId(eq(1L))).thenReturn(List.of(item));
        when(commentRepository.findByItemIdIn(anyList())).thenReturn(Collections.emptyList());
        when(bookingRepository.findByItemIdInAndStatus(anyList(), eq(BookingStatus.APPROVED)))
                .thenReturn(Collections.emptyList());

        List<ItemResponseDto> result = itemService.getByOwnerId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getByOwnerId_WithNoItems_ShouldReturnEmptyList() {
        when(userRepository.existsById(eq(1L))).thenReturn(true);
        when(itemRepository.findByOwnerId(eq(1L))).thenReturn(Collections.emptyList());

        List<ItemResponseDto> result = itemService.getByOwnerId(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getByOwnerId_NonExistentUser_ShouldThrowException() {
        when(userRepository.existsById(eq(999L))).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.getByOwnerId(999L));
    }

    @Test
    void search_ShouldFindItems() {
        when(itemRepository.search(anyString())).thenReturn(List.of(item));

        List<ItemDto> result = itemService.search("Item");

        assertEquals(1, result.size());
        assertEquals("Item", result.get(0).getName());
    }

    @Test
    void search_WithEmptyText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.search("");

        assertTrue(result.isEmpty());
    }

    @Test
    void search_WithNullText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.search(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void addComment_ShouldReturnCommentDto() {
        when(userRepository.findById(eq(2L))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(bookingRepository.findCompletedBookingsByItemAndUser(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(new Booking()));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.addComment(2L, 1L, "Great item!");

        assertNotNull(result.getId());
        assertEquals("Great item!", result.getText());
        assertEquals("Booker", result.getAuthorName());
    }

    @Test
    void addComment_WithoutBooking_ShouldThrowException() {
        when(userRepository.findById(eq(2L))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(bookingRepository.findCompletedBookingsByItemAndUser(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class,
                () -> itemService.addComment(2L, 1L, "Great item!"));
    }

    @Test
    void addComment_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.findById(eq(999L))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.addComment(999L, 1L, "Great item!"));
    }

    @Test
    void addComment_WithNonExistentItem_ShouldThrowException() {
        when(userRepository.findById(eq(2L))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(eq(999L))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.addComment(2L, 999L, "Great item!"));
    }
}
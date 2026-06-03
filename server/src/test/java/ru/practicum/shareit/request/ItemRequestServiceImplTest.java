package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Need a drill");
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("Need a drill");
        itemRequestDto.setCreated(LocalDateTime.now());
    }

    @Test
    void create_ShouldReturnItemRequestDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto result = itemRequestService.create(1L, new ItemRequestCreateDto("Need a drill"));

        assertNotNull(result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertNotNull(result.getCreated());
    }

    @Test
    void create_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.create(999L, new ItemRequestCreateDto("Need a drill")));
    }

    @Test
    void getByUserId_ShouldReturnRequests() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRequestRepository.findByRequestorId(anyLong(), any(Sort.class)))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestId(anyLong())).thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = itemRequestService.getByUserId(1L);

        assertEquals(1, result.size());
        assertEquals("Need a drill", result.get(0).getDescription());
    }

    @Test
    void getByUserId_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemRequestService.getByUserId(999L));
    }

    @Test
    void getAllExceptUser_ShouldReturnRequests() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRequestRepository.findByRequestorIdNot(anyLong(), any(Sort.class)))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestId(anyLong())).thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = itemRequestService.getAllExceptUser(2L);

        assertEquals(1, result.size());
    }

    @Test
    void getById_ShouldReturnRequestWithItems() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));

        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Electric drill");
        item.setAvailable(true);

        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of(item));

        ItemRequestDto result = itemRequestService.getById(1L, 1L);

        assertNotNull(result);
        assertEquals("Need a drill", result.getDescription());
        assertEquals(1, result.getItems().size());
        assertEquals("Drill", result.getItems().get(0).getName());
    }

    @Test
    void getById_WithNonExistentRequest_ShouldThrowException() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getById(999L, 1L));
    }
}
package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto requestor;
    private UserDto owner;
    private ItemRequestDto request;

    @BeforeEach
    void setUp() {
        requestor = userService.create(new UserDto(null, "Requestor", "requestor@example.com"));
        owner = userService.create(new UserDto(null, "Owner", "owner@example.com"));

        request = itemRequestService.create(requestor.getId(),
                new ItemRequestCreateDto("Need a drill"));
    }

    @Test
    void create_ShouldReturnItemRequestDto() {
        ItemRequestDto created = itemRequestService.create(requestor.getId(),
                new ItemRequestCreateDto("Need a hammer"));

        assertNotNull(created.getId());
        assertEquals("Need a hammer", created.getDescription());
        assertNotNull(created.getCreated());
    }

    @Test
    void create_WithNonExistentUser_ShouldThrowException() {
        assertThrows(NotFoundException.class, () ->
                itemRequestService.create(999L, new ItemRequestCreateDto("Need a hammer")));
    }

    @Test
    void getByUserId_ShouldReturnUserRequests() {
        List<ItemRequestDto> requests = itemRequestService.getByUserId(requestor.getId());

        assertEquals(1, requests.size());
        assertEquals("Need a drill", requests.get(0).getDescription());
    }

    @Test
    void getAllExceptUser_ShouldReturnOtherRequests() {
        List<ItemRequestDto> requests = itemRequestService.getAllExceptUser(owner.getId());

        assertEquals(1, requests.size());
        assertEquals("Need a drill", requests.get(0).getDescription());
    }

    @Test
    void getById_ShouldReturnRequestWithItems() {
        // Добавляем вещь в ответ на запрос
        itemService.create(owner.getId(),
                new ItemDto(null, "Drill", "Electric drill", true, request.getId()));

        ItemRequestDto found = itemRequestService.getById(request.getId(), requestor.getId());

        assertNotNull(found);
        assertEquals("Need a drill", found.getDescription());
        assertEquals(1, found.getItems().size());
        assertEquals("Drill", found.getItems().get(0).getName());
    }

    @Test
    void getById_WithNonExistentRequest_ShouldThrowException() {
        assertThrows(NotFoundException.class, () ->
                itemRequestService.getById(999L, requestor.getId()));
    }
}
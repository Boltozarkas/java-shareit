package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {

    @Test
    void toItemRequestDto_ShouldMapAllFields() {
        User requestor = new User();
        requestor.setId(1L);
        requestor.setName("Requestor");

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need a drill");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemDto itemDto = new ItemDto(1L, "Drill", "Electric drill", true, 1L);
        List<ItemDto> items = List.of(itemDto);

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request, items);

        assertEquals(1L, dto.getId());
        assertEquals("Need a drill", dto.getDescription());
        assertNotNull(dto.getCreated());
        assertEquals(1, dto.getItems().size());
        assertEquals("Drill", dto.getItems().get(0).getName());
    }

    @Test
    void toItemRequestDto_WithNullItems_ShouldWork() {
        User requestor = new User();
        requestor.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need a drill");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request, null);

        assertEquals(1L, dto.getId());
        assertEquals("Need a drill", dto.getDescription());
        assertNull(dto.getItems());
    }
}
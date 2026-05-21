package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemResponseDto getById(Long itemId, Long userId);

    List<ItemResponseDto> getByOwnerId(Long userId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, String text);
}
package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final Map<Long, Item> items = new HashMap<>();
    private final UserService userService;
    private long idCounter = 1;

    public ItemServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        // Проверка обязательных полей
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (itemDto.getAvailable() == null) {
            throw new IllegalArgumentException("Available is required");
        }

        // Проверка существования пользователя
        try {
            userService.getById(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("User not found: " + userId);
        }

        User owner = new User();
        owner.setId(userId);

        Item item = ItemMapper.toItem(itemDto);
        item.setId(idCounter++);
        item.setOwner(owner);
        items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item existingItem = items.get(itemId);
        if (existingItem == null) {
            throw new NotFoundException("Item not found: " + itemId);
        }
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Item not found: " + itemId);
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(existingItem);
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Item not found: " + itemId);
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getByOwnerId(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item ->
                        (item.getName() != null && item.getName().toLowerCase().contains(lowerText)) ||
                                (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerText)))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(dto.getDescription());
        itemRequest.setRequestor(requestor);

        ItemRequest saved = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(saved, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }

        List<ItemRequest> requests = itemRequestRepository.findByRequestorId(
                userId, Sort.by(Sort.Direction.DESC, "created"));

        return toItemRequestDtoList(requests);
    }

    @Override
    public List<ItemRequestDto> getAllExceptUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNot(
                userId, Sort.by(Sort.Direction.DESC, "created"));

        return toItemRequestDtoList(requests);
    }

    @Override
    public ItemRequestDto getById(Long requestId, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));

        List<Item> items = itemRepository.findByRequestId(requestId);
        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        return ItemRequestMapper.toItemRequestDto(request, itemDtos);
    }

    private List<ItemRequestDto> toItemRequestDtoList(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        // Собираем все ID запросов
        Set<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toSet());

        // Загружаем все вещи для всех запросов одним запросом
        List<Item> allItems = itemRepository.findByRequestIdIn(new ArrayList<>(requestIds));

        // Группируем вещи по ID запроса
        Map<Long, List<ItemDto>> itemsByRequestId = allItems.stream()
                .collect(Collectors.groupingBy(
                        Item::getRequestId,
                        Collectors.mapping(ItemMapper::toItemDto, Collectors.toList())
                ));

        // Формируем DTO для каждого запроса
        return requests.stream()
                .map(request -> {
                    List<ItemDto> items = itemsByRequestId.getOrDefault(
                            request.getId(), Collections.emptyList());
                    return ItemRequestMapper.toItemRequestDto(request, items);
                })
                .collect(Collectors.toList());
    }
}
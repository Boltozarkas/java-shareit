package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

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

        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest), List.of());
    }

    @Override
    public List<ItemRequestDto> getByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }

        return itemRequestRepository.findByRequestorId(userId, Sort.by(Sort.Direction.DESC, "created"))
                .stream()
                .map(request -> {
                    List<ItemDto> items = itemRepository.findByRequestId(request.getId())
                            .stream()
                            .map(ItemMapper::toItemDto)
                            .toList();
                    return ItemRequestMapper.toItemRequestDto(request, items);
                })
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAllExceptUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }

        return itemRequestRepository.findByRequestorIdNot(userId, Sort.by(Sort.Direction.DESC, "created"))
                .stream()
                .map(request -> {
                    List<ItemDto> items = itemRepository.findByRequestId(request.getId())
                            .stream()
                            .map(ItemMapper::toItemDto)
                            .toList();
                    return ItemRequestMapper.toItemRequestDto(request, items);
                })
                .toList();
    }

    @Override
    public ItemRequestDto getById(Long requestId, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));

        List<ItemDto> items = itemRepository.findByRequestId(requestId)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();

        return ItemRequestMapper.toItemRequestDto(request, items);
    }
}
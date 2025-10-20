package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceIml implements ItemRequestService {
    private final Map<Long, ItemRequest> itemRequests = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final UserService userService;

    @Override
    public ItemRequestDto createRequest(ItemRequestDto itemRequestDto, Long requestorId) {
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание запроса не может быть пустым");
        }

        User requestor = userService.getUserEntityById(requestorId);

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, requestor);
        itemRequest.setId(idCounter.getAndIncrement());
        itemRequest.setCreated(LocalDateTime.now());

        itemRequests.put(itemRequest.getId(), itemRequest);
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getAllRequestsByRequestor(Long requestorId) {
        userService.getUserEntityById(requestorId);

        return itemRequests.values().stream()
                .filter(request -> request.getRequestor().getId().equals(requestorId))
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        userService.getUserEntityById(userId);

        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Параметры пагинации должны быть from >= 0 и size > 0");
        }

        return itemRequests.values().stream()
                .filter(request -> !request.getRequestor().getId().equals(userId))
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .skip(from)
                .limit(size)
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        userService.getUserEntityById(userId);

        ItemRequest itemRequest = itemRequests.get(requestId);
        if (itemRequest == null) {
            throw new RuntimeException("Запрос не найден");
        }

        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }
}

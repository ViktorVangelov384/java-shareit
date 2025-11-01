package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;

@UtilityClass
public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return toItemRequestDto(itemRequest, Collections.emptyList());
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest, List<ItemDto> items) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getRequestor() != null ? itemRequest.getRequestor().getId() : null,
                itemRequest.getCreated(),
                items
        );
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User requestor) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(itemRequestDto.getId());
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(itemRequestDto.getCreated() != null ? itemRequestDto.getCreated() : java.time.LocalDateTime.now());
        return itemRequest;
    }
}


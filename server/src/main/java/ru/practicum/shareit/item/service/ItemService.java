package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemWithBookingsDto getItemByIdWithBookings(Long itemId, Long userId);

    List<ItemWithBookingsDto> getAllItemsByOwner(Long ownerId, int from, int size);

    List<ItemDto> searchItems(String text, int from, int size);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);

}

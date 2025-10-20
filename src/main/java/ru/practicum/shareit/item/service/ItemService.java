package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemDto getItemById(Long itemId);

    Item getItemEntityById(Long itemId);

    List<ItemDto> getAllItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text);

}

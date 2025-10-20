package ru.practicum.shareit.item.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceIml implements ItemService {
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final UserService userService;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        User owner = userService.getUserEntityById(ownerId);
        Item item = ItemMapper.toItem(itemDto, owner);
        item.setId(idCounter.getAndIncrement());
        items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = items.get(itemId);
        if (existingItem == null) {
            throw new NotFoundException("Продукт не найден");
        }

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Только собственник может обновлять продукт");
        }

        ItemMapper.updateItemFromDto(itemDto, existingItem);

        items.put(itemId, existingItem);
        return ItemMapper.toItemDto(existingItem);

    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("ПРодукт не найден");
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public Item getItemEntityById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Продукт не найден");
        }
        return item;
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(searchText) ||
                        item.getDescription().toLowerCase().contains(searchText))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}

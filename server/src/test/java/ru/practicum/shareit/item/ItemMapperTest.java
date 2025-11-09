package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void toItemDto_shouldMapCorrectly() {
        User owner = new User(1L, "Owner", "owner@email.com");
        Item item = new Item(1L, "Дрель", "Описание", true, owner, 10L);

        ItemDto result = ItemMapper.toItemDto(item);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getDescription()).isEqualTo("Описание");
        assertThat(result.getAvailable()).isEqualTo(true);
        assertThat(result.getRequestId()).isEqualTo(10L);
    }

    @Test
    void toItem_shouldMapCorrectly() {
        User owner = new User(1L, "Owner", "owner@email.com");
        ItemDto itemDto = new ItemDto(null, "Дрель", "Описание", true, 10L);

        Item result = ItemMapper.toItem(itemDto, owner);

        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getDescription()).isEqualTo("Описание");
        assertThat(result.getAvailable()).isEqualTo(true);
        assertThat(result.getOwner()).isEqualTo(owner);
        assertThat(result.getRequestId()).isEqualTo(10L);
    }
}

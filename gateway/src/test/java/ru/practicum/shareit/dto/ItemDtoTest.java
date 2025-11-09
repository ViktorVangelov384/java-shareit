package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.junit.jupiter.api.Assertions.*;

class ItemDtoTest {

    @Test
    void itemDto_NoArgsConstructor_ShouldCreateObject() {
        ItemDto item = new ItemDto();
        assertNotNull(item);
    }

    @Test
    void itemDto_AllArgsConstructor_ShouldSetFields() {
        ItemDto item = new ItemDto(1L, "Name", "Description", true, 100L);

        assertEquals(1L, item.getId());
        assertEquals("Name", item.getName());
        assertEquals("Description", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(100L, item.getRequestId());
    }

    @Test
    void itemDto_SettersAndGetters_ShouldWork() {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Name");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setRequestId(100L);

        assertEquals(1L, item.getId());
        assertEquals("Name", item.getName());
        assertEquals("Description", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(100L, item.getRequestId());
    }
}

package ru.practicum.shareit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.ItemClient;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Test
    void getItemById_shouldCallClient() throws Exception {
        when(itemClient.getItemById(anyLong(), anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_shouldCallClient() throws Exception {
        when(itemClient.searchItems(anyString(), anyInt(), anyInt())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItemsByOwner_shouldCallClient() throws Exception {
        when(itemClient.getAllItemsByOwner(anyLong(), anyInt(), anyInt())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void createItem_WithValidData_ShouldReturnOk() throws Exception {
        when(itemClient.createItem(any(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        String itemJson = "{" +
                "\"name\":\"Дрель\"," +
                "\"description\":\"Аккумуляторная дрель\"," +
                "\"available\":true" +
                "}";

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_ShouldCallClient() throws Exception {
        when(itemClient.updateItem(anyLong(), any(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        String itemJson = "{\"name\":\"Updated Drill\"}";

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_ShouldCallClient() throws Exception {
        when(itemClient.addComment(anyLong(), anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        String commentJson = "{\"text\":\"Отличная дрель!\"}";

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson))
                .andExpect(status().isOk());
    }

    @Test
    void createItem_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        String invalidItemJson = "{" +
                "\"name\":\"\"," +
                "\"description\":\"\"," +
                "\"available\":null" +
                "}";

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidItemJson))
                .andExpect(status().isBadRequest());
    }
}
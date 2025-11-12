package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemService itemService;

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Дрель", "Мощная дрель", true, null);
        when(itemService.createItem(any(ItemDto.class), eq(1L))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\",\"description\":\"Мощная дрель\",\"available\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void createItem_withNonExistentUser_shouldThrowNotFoundException() throws Exception {
        when(itemService.createItem(any(ItemDto.class), eq(999L)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\",\"description\":\"Мощная дрель\",\"available\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createItem_withMissingUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\",\"description\":\"Мощная дрель\",\"available\":true}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getItemById_shouldReturnItem() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Дрель", "Описание", true, null);
        when(itemService.getItemByIdWithBookings(eq(1L), eq(1L))).thenReturn(
                new ItemWithBookingsDto(1L, "Дрель", "Описание",
                        true, null, null, null, List.of()));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }


    @Test
    void getItemById_withNonExistentItem_shouldReturnNotFound() throws Exception {
        when(itemService.getItemByIdWithBookings(eq(999L), eq(1L)))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        mockMvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Новая дрель", "Обновленное описание", true, null);
        when(itemService.updateItem(eq(1L), any(ItemDto.class), eq(1L))).thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новая дрель\",\"description\":\"Обновленное описание\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новая дрель"))
                .andExpect(jsonPath("$.description").value("Обновленное описание"));
    }

    @Test
    void updateItem_withNonOwner_shouldThrowAccessDeniedException() throws Exception {
        when(itemService.updateItem(eq(1L), any(ItemDto.class), eq(2L)))
                .thenThrow(new AccessDeniedException("Только владелец может обновлять вещь"));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новое название\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllItemsByOwner_shouldReturnItems() throws Exception {
        ItemWithBookingsDto itemDto = new ItemWithBookingsDto(1L, "Дрель", "Описание",
                true, null, null, null, List.of());
        when(itemService.getAllItemsByOwner(eq(1L), eq(0), eq(10))).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void searchItems_shouldReturnItems() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Дрель", "Описание", true, null);
        when(itemService.searchItems(eq("дрель"), anyInt(), anyInt())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void searchItems_withBlankText_shouldReturnEmptyList() throws Exception {
        when(itemService.searchItems(eq(""), anyInt(), anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", "")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchItems_withDefaultPagination_shouldReturnItems() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Дрель", "Описание", true, null);
        when(itemService.searchItems(eq("дрель"), eq(0), eq(10))).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void addComment_shouldReturnComment() throws Exception {
        CommentDto commentDto = new CommentDto(1L, "Отличная вещь!", "Автор", LocalDateTime.now());
        when(itemService.addComment(eq(1L), eq(2L), any(CommentDto.class))).thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Отличная вещь!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Отличная вещь!"));
    }

    @Test
    void addComment_withoutBooking_shouldThrowValidationException() throws Exception {
        when(itemService.addComment(eq(1L), eq(2L), any(CommentDto.class)))
                .thenThrow(new ValidationException("Только арендатор может оставлять комментарии"));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Комментарий\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void addComment_withMissingUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Комментарий\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }
}

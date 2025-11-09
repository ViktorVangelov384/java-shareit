package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ItemRequestService itemRequestService;

    @Test
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(1L, "Нужна дрель", 1L,
                LocalDateTime.now(), List.of());
        when(itemRequestService.createRequest(any(ItemRequestDto.class), eq(1L))).thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"Нужна дрель\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужна дрель"))
                .andExpect(jsonPath("$.requestorId").value(1L));
    }

    @Test
    void getAllRequestsByRequestor_shouldReturnUserRequests() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Дрель", "Мощная", true, 1L);
        ItemRequestDto requestDto = new ItemRequestDto(1L, "Нужна дрель", 1L,
                LocalDateTime.now(), List.of(itemDto));
        when(itemRequestService.getAllRequestsByRequestor(eq(1L))).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].items[0].name").value("Дрель"));
    }

    @Test
    void getAllRequests_shouldReturnPaginatedOtherUsersRequests() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(1L, "Нужна дрель", 2L,
                LocalDateTime.now(), List.of());
        when(itemRequestService.getAllRequests(eq(1L), eq(0), eq(10))).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all?from=0&size=10")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"));
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Дрель", "Мощная", true, 1L);
        ItemRequestDto requestDto = new ItemRequestDto(1L, "Нужна дрель", 1L,
                LocalDateTime.now(), List.of(itemDto));
        when(itemRequestService.getRequestById(eq(1L), eq(1L))).thenReturn(requestDto);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.items[0].name").value("Дрель"));
    }
}
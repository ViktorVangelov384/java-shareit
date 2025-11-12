package ru.practicum.shareit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.BookingClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void createBooking_WithValidData_ShouldReturnOk() throws Exception {
        when(bookingClient.createBooking(any(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        String bookingJson = "{" +
                "\"start\":\"" + start.format(formatter) + "\"," +
                "\"end\":\"" + end.format(formatter) + "\"," +
                "\"itemId\":1" +
                "}";

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_WithPastDates_ShouldReturnBadRequest() throws Exception {
        String invalidBookingJson = "{" +
                "\"start\":\"2024-01-01T10:00:00\"," +
                "\"end\":\"2024-01-02T10:00:00\"," +
                "\"itemId\":1" +
                "}";

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBookingJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_ShouldCallClient() throws Exception {
        when(bookingClient.approveBooking(anyLong(), anyBoolean(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllBookingsByBooker_WithDifferentStates_ShouldCallClient() throws Exception {
        when(bookingClient.getAllBookingsByBooker(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok().build());

        String[] states = {"CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"};

        for (String state : states) {
            mockMvc.perform(get("/bookings")
                            .header("X-Sharer-User-Id", 1L)
                            .param("state", state))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void getAllBookingsByBooker_shouldCallClient() throws Exception {
        when(bookingClient.getAllBookingsByBooker(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllBookingsByOwner_shouldCallClient() throws Exception {
        when(bookingClient.getAllBookingsByOwner(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingById_shouldCallClient() throws Exception {
        when(bookingClient.getBookingById(anyLong(), anyLong())).thenReturn(null);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}

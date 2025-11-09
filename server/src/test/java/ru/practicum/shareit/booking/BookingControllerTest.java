package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private BookingService bookingService;

    @Test
    void createBooking_shouldReturnCreatedBooking() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Дрель"),
                new BookingResponseDto.Booker(2L),
                "WAITING"
        );

        when(bookingService.createBooking(any(BookingDto.class), eq(2L))).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"start\":\"2023-12-01T10:00:00\",\"end\":\"2023-12-02T10:00:00\",\"itemId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getBookingById_shouldReturnBooking() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Дрель"),
                new BookingResponseDto.Booker(2L),
                "APPROVED"
        );

        when(bookingService.getBookingById(eq(1L), eq(2L))).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getAllBookingsByBooker_shouldReturnBookings() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Дрель"),
                new BookingResponseDto.Booker(2L),
                "WAITING"
        );

        when(bookingService.getAllBookingsByBooker(eq(2L), eq("ALL")))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void approveBooking_shouldReturnApprovedBooking() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Дрель"),
                new BookingResponseDto.Booker(2L),
                "APPROVED"
        );

        when(bookingService.approveBooking(eq(1L), eq(1L), eq(true))).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingById_withNonExistentBooking_shouldReturnNotFound() throws Exception {
        when(bookingService.getBookingById(eq(999L), eq(1L)))
                .thenThrow(new NotFoundException("Бронирование не найдено"));

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBookingsByBooker_withDifferentStates_shouldReturnBookings() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Дрель"), new BookingResponseDto.Booker(2L), "WAITING");

        String[] states = {"CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"};

        for (String state : states) {
            when(bookingService.getAllBookingsByBooker(eq(2L), eq(state)))
                    .thenReturn(List.of(responseDto));

            mockMvc.perform(get("/bookings")
                            .header("X-Sharer-User-Id", 2L)
                            .param("state", state))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L));
        }
    }

    @Test
    void approveBooking_withFalse_shouldReturnRejectedBooking() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Дрель"), new BookingResponseDto.Booker(2L), "REJECTED");

        when(bookingService.approveBooking(eq(1L), eq(1L), eq(false))).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

}

package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingDtoTest {

    @Test
    void bookingDto_NoArgsConstructor_ShouldCreateObject() {
        BookingDto booking = new BookingDto();
        assertNotNull(booking);
    }

    @Test
    void bookingDto_AllArgsConstructor_ShouldSetFields() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        BookingDto booking = new BookingDto(1L, start, end, 100L, 200L, "WAITING");

        assertEquals(1L, booking.getId());
        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(100L, booking.getItemId());
        assertEquals(200L, booking.getBookerId());
        assertEquals("WAITING", booking.getStatus());
    }

    @Test
    void bookingDto_SettersAndGetters_ShouldWork() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        BookingDto booking = new BookingDto();
        booking.setId(1L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItemId(100L);
        booking.setBookerId(200L);
        booking.setStatus("APPROVED");

        assertEquals(1L, booking.getId());
        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(100L, booking.getItemId());
        assertEquals(200L, booking.getBookerId());
        assertEquals("APPROVED", booking.getStatus());
    }
}

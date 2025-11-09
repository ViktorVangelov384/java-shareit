package ru.practicum.shareit.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(BookingClient.class)
class BookingClientTest {

    @Autowired
    private BookingClient bookingClient;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void createBooking_ShouldCallPost() {
        server.expect(requestTo("http://localhost:9090/bookings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andExpect(content().contentType("application/json"))
                .andRespond(withSuccess("{}", org.springframework.http.MediaType.APPLICATION_JSON));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItemId(1L);

        ResponseEntity<Object> response = bookingClient.createBooking(bookingDto, 1L);

        assertNotNull(response);
        server.verify();
    }

    @Test
    void approveBooking_ShouldCallPatch() {
        server.expect(requestTo("http://localhost:9090/bookings/1?approved=true"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{}", org.springframework.http.MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = bookingClient.approveBooking(1L, true, 1L);

        assertNotNull(response);
        server.verify();
    }

    @Test
    void getBookingById_ShouldCallGet() {
        server.expect(requestTo("http://localhost:9090/bookings/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{}", org.springframework.http.MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = bookingClient.getBookingById(1L, 1L);

        assertNotNull(response);
        server.verify();
    }

    @Test
    void getAllBookingsByBooker_ShouldCallGet() {
        server.expect(requestTo("http://localhost:9090/bookings?state=ALL"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{}", org.springframework.http.MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = bookingClient.getAllBookingsByBooker(1L, "ALL");

        assertNotNull(response);
        server.verify();
    }

    @Test
    void getAllBookingsByOwner_ShouldCallGet() {
        server.expect(requestTo("http://localhost:9090/bookings/owner?state=ALL"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{}", org.springframework.http.MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = bookingClient.getAllBookingsByOwner(1L, "ALL");

        assertNotNull(response);
        server.verify();
    }
}
package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void testBookingDtoSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2023, 12, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 12, 2, 10, 0);
        BookingDto bookingDto = new BookingDto(1L, start, end, 1L, 2L, "WAITING");

        var result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2023-12-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2023-12-02T10:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }

    @Test
    void testBookingDtoDeserialize() throws Exception {
        String content = "{\"id\":1,\"start\":\"2023-12-01T10:00:00\"," +
                "\"end\":\"2023-12-02T10:00:00\",\"itemId\":1,\"bookerId\":2,\"status\":\"WAITING\"}";

        BookingDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2023,
                12, 1, 10, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2023,
                12, 2, 10, 0));
        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getBookerId()).isEqualTo(2L);
        assertThat(result.getStatus()).isEqualTo("WAITING");
    }
}
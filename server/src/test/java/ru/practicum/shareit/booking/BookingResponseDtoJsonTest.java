package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingResponseDtoJsonTest {

    @Autowired
    private JacksonTester<BookingResponseDto> json;

    @Test
    void testBookingResponseDtoSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2023, 12, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 12, 2, 10, 0);
        BookingResponseDto.Item item = new BookingResponseDto.Item(1L, "Дрель");
        BookingResponseDto.Booker booker = new BookingResponseDto.Booker(2L);
        BookingResponseDto responseDto = new BookingResponseDto(1L, start, end, item, booker, "APPROVED");

        var result = json.write(responseDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2023-12-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2023-12-02T10:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
    }

    @Test
    void testBookingResponseDtoDeserialize() throws Exception {
        String content = "{\"id\":1,\"start\":\"2023-12-01T10:00:00\",\"end\":\"2023-12-02T10:00:00\"," +
                "\"item\":{\"id\":1,\"name\":\"Дрель\"},\"booker\":{\"id\":2},\"status\":\"APPROVED\"}";

        BookingResponseDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2023,
                12, 1, 10, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2023,
                12, 2, 10, 0));
        assertThat(result.getItem().getId()).isEqualTo(1L);
        assertThat(result.getItem().getName()).isEqualTo("Дрель");
        assertThat(result.getBooker().getId()).isEqualTo(2L);
        assertThat(result.getStatus()).isEqualTo("APPROVED");
    }
}

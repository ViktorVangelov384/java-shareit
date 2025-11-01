package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;


import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class BookingMapper {

    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        BookingResponseDto.Item itemDto = new BookingResponseDto.Item(
                booking.getItem().getId(),
                booking.getItem().getName()
        );

        BookingResponseDto.Booker bookerDto = new BookingResponseDto.Booker(
                booking.getBooker().getId()
        );

        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                itemDto,
                bookerDto,
                booking.getStatus().name()
        );
    }

    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem() != null ? booking.getItem().getId() : null,
                booking.getBooker() != null ? booking.getBooker().getId() : null,
                booking.getStatus().name()
        );
    }

    public static Booking toBooking(BookingDto bookingDto, Item item, User booker) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);

        if (bookingDto.getStatus() != null) {
            try {
                booking.setStatus(BookingStatus.valueOf(bookingDto.getStatus()));
            } catch (IllegalArgumentException e) {
                booking.setStatus(BookingStatus.WAITING);
            }
        } else {
            booking.setStatus(BookingStatus.WAITING);
        }

        return booking;
    }
}

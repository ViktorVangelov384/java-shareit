package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestBody BookingDto bookingDto,
                                    @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        return bookingService.createBooking(bookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@PathVariable Long bookingId,
                                     @RequestParam boolean approved,
                                     @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return bookingService.approveBooking(bookingId, ownerId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable Long bookingId,
                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAllBookingsByBooker(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                                                   @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllBookingsByBooker(bookerId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllBookingsByOwner(ownerId, state);
    }
}

package ru.practicum.shareit.booking.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceIml implements BookingService {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public BookingDto createBooking(BookingDto bookingDto, Long bookerId) {
        validateBookingDates(bookingDto);

        User booker = userService.getUserEntityById(bookerId);
        Item item = getItemEntityById(bookingDto.getItemId());

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            throw new RuntimeException("Владелец не может бронировать свою вещь");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        booking.setId(idCounter.getAndIncrement());
        booking.setStatus(BookingStatus.WAITING);
        bookings.put(booking.getId(), booking);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new RuntimeException("Бронирование не найдено");
        }

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Только владелец вещи может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookings.put(bookingId, booking);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new RuntimeException("Бронирование не найдено");
        }

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new RuntimeException("Доступ к бронированию запрещен");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookingsByBooker(Long bookerId, String state) {
        userService.getUserEntityById(bookerId);

        return bookings.values().stream()
                .filter(booking -> booking.getBooker().getId().equals(bookerId))
                .filter(booking -> filterByState(booking, state))
                .sorted((b1, b2) -> b2.getStart().compareTo(b1.getStart()))
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllBookingsByOwner(Long ownerId, String state) {
        userService.getUserEntityById(ownerId);

        return bookings.values().stream()
                .filter(booking -> booking.getItem().getOwner().getId().equals(ownerId))
                .filter(booking -> filterByState(booking, state))
                .sorted((b1, b2) -> b2.getStart().compareTo(b1.getStart()))
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private void validateBookingDates(BookingDto bookingDto) {
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new IllegalArgumentException("Дата начала и окончания обязательны");
        }

        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }

        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Дата начала должна быть в будущем");
        }

        if (bookingDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Дата окончания должна быть в будущем");
        }
    }

    private boolean filterByState(Booking booking, String state) {
        LocalDateTime now = LocalDateTime.now();
        String stateUpper = state.toUpperCase();

        switch (stateUpper) {
            case "ALL":
                return true;
            case "CURRENT":
                return booking.getStart().isBefore(now) && booking.getEnd().isAfter(now);
            case "PAST":
                return booking.getEnd().isBefore(now);
            case "FUTURE":
                return booking.getStart().isAfter(now);
            case "WAITING":
                return booking.getStatus() == BookingStatus.WAITING;
            case "REJECTED":
                return booking.getStatus() == BookingStatus.REJECTED;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    private Item getItemEntityById(Long itemId) {
        User owner = new User(1L, "Owner", "owner@example.com");
        Item item = new Item(itemId, "Item " + itemId, "Description " + itemId, true, owner, null);

        if (item == null) {
            throw new RuntimeException("Вещь не найдена с ID: " + itemId);
        }

        return item;
    }

    public void clearBookings() {
        bookings.clear();
    }

    public int getBookingsCount() {
        return bookings.size();
    }

}

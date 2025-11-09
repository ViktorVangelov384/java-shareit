package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceIml;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceUnitTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceIml bookingService;

    @Test
    void createBooking_withNonExistentUser_shouldThrowNotFoundException() {
        BookingDto bookingDto = new BookingDto(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L, null, null);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    void createBooking_withNonExistentItem_shouldThrowNotFoundException() {
        BookingDto bookingDto = new BookingDto(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                999L, null, null);
        User booker = new User(1L, "Booker", "booker@email.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Вещь не найдена");
    }

    @Test
    void createBooking_withUnavailableItem_shouldThrowValidationException() {
        BookingDto bookingDto = new BookingDto(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L, null, null);
        User booker = new User(1L, "Booker", "booker@email.com");
        User owner = new User(2L, "Owner", "owner@email.com");
        Item item = new Item(1L, "Дрель", "Описание", false, owner, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Вещь недоступна для бронирования");
    }

    @Test
    void createBooking_byOwner_shouldThrowNotFoundException() {
        BookingDto bookingDto = new BookingDto(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L, null, null);
        User owner = new User(1L, "Owner", "owner@email.com");
        Item item = new Item(1L, "Дрель", "Описание", true, owner, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Владелец не может бронировать свою вещь");
    }

    @Test
    void createBooking_withEndBeforeStart_shouldThrowIllegalArgumentException() {
        BookingDto bookingDto = new BookingDto(null,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1),
                1L, null, null);


        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Дата начала не может быть позже даты окончания");
    }

    @Test
    void createBooking_withInvalidDates_shouldThrowIllegalArgumentException() {
        BookingDto bookingDto = new BookingDto(null,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                1L, null, null);

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Дата начала должна быть в будущем");
    }

    @Test
    void createBooking_withValidData_shouldCreateBooking() {
        BookingDto bookingDto = new BookingDto(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L, null, null);
        User booker = new User(1L, "Booker", "booker@email.com");
        User owner = new User(2L, "Owner", "owner@email.com");
        Item item = new Item(1L, "Дрель", "Описание", true, owner, null);
        Booking savedBooking = new Booking(1L, bookingDto.getStart(), bookingDto.getEnd(),
                item, booker, BookingStatus.WAITING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingResponseDto result = bookingService.createBooking(bookingDto, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("WAITING");
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void getBookingById_withUnauthorizedUser_shouldThrowAccessDeniedException() {
        User owner = new User(1L, "Owner", "owner@email.com");
        User booker = new User(2L, "Booker", "booker@email.com");
        User otherUser = new User(3L, "Other", "other@email.com");
        Item item = new Item(1L, "Дрель", "Описание", true, owner, null);
        Booking booking = new Booking(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(1L, 3L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Доступ к бронированию запрещен");
    }


    @Test
    void approveBooking_byNonOwner_shouldThrowValidationException() {
        User owner = new User(1L, "Owner", "owner@email.com");
        User booker = new User(2L, "Booker", "booker@email.com");
        User otherUser = new User(3L, "Other", "other@email.com");
        Item item = new Item(1L, "Дрель", "Описание", true, owner, null);
        Booking booking = new Booking(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.existsById(3L)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 3L, true))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Только владелец вещи может подтверждать бронирование");
    }

    @Test
    void approveBooking_withAlreadyApprovedBooking_shouldThrowValidationException() {
        User owner = new User(1L, "Owner", "owner@email.com");
        User booker = new User(2L, "Booker", "booker@email.com");
        Item item = new Item(1L, "Дрель", "Описание", true, owner, null);
        Booking booking = new Booking(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.APPROVED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 1L, true))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Бронирование уже обработано");
    }

    @Test
    void approveBooking_withAlreadyRejectedBooking_shouldThrowValidationException() {
        User owner = new User(1L, "Owner", "owner@email.com");
        User booker = new User(2L, "Booker", "booker@email.com");
        Item item = new Item(1L, "Дрель", "Описание", true, owner, null);
        Booking booking = new Booking(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.REJECTED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 1L, true))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Бронирование уже обработано");
    }
}

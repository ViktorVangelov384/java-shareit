package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceIml;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(BookingServiceIml.class)
class BookingServiceIntegrationTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private BookingServiceIml bookingService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User(null, "Owner", "owner@email.com");
        booker = new User(null, "Booker", "booker@email.com");
        userRepository.save(owner);
        userRepository.save(booker);

        item = new Item(null, "Дрель", "Мощная дрель", true, owner, null);
        itemRepository.save(item);

        booking = new Booking(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.WAITING);
        bookingRepository.save(booking);
    }

    @Test
    void createBooking_shouldSaveBookingToDatabase() {
        ru.practicum.shareit.booking.dto.BookingDto bookingDto =
                new ru.practicum.shareit.booking.dto.BookingDto(null,
                        LocalDateTime.now().plusDays(3),
                        LocalDateTime.now().plusDays(4),
                        item.getId(), null, null);

        BookingResponseDto result = bookingService.createBooking(bookingDto, booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING.toString());

        Booking savedBooking = em.find(Booking.class, result.getId());
        assertThat(savedBooking).isNotNull();
        assertThat(savedBooking.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void getBookingById_shouldReturnBooking() {
        BookingResponseDto result = bookingService.getBookingById(booking.getId(), booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(booking.getId());
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void getAllBookingsByBooker_shouldReturnUserBookings() {
        List<BookingResponseDto> result = bookingService.getAllBookingsByBooker(booker.getId(), "ALL");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    void getAllBookingsByOwner_shouldReturnOwnerBookings() {
        List<BookingResponseDto> result = bookingService.getAllBookingsByOwner(owner.getId(), "ALL");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    void approveBooking_shouldUpdateBookingStatus() {
        BookingResponseDto result = bookingService.approveBooking(booking.getId(), owner.getId(), true);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED.toString());

        Booking updatedBooking = em.find(Booking.class, booking.getId());
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }
}

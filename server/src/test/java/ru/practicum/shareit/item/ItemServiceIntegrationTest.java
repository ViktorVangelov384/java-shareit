package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceIml;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ItemServiceIml.class)
class ItemServiceIntegrationTest {

    @Autowired private TestEntityManager em;
    @Autowired private ItemServiceIml itemService;
    @Autowired private UserRepository userRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private CommentRepository commentRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User(null, "Owner", "owner@email.com");
        booker = new User(null, "Booker", "booker@email.com");
        userRepository.save(owner);
        userRepository.save(booker);

        item = new Item(null, "Дрель", "Мощная дрель", true, owner, null);
        itemRepository.save(item);
    }

    @Test
    void createItem_shouldSaveItemToDatabase() {
        ItemDto itemDto = new ItemDto(null, "Отвертка", "Крестовая отвертка",
                true, null);

        ItemDto result = itemService.createItem(itemDto, owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Отвертка");
        assertThat(result.getDescription()).isEqualTo("Крестовая отвертка");

        Item savedItem = em.find(Item.class, result.getId());
        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void getItemByIdWithBookings_shouldReturnItemWithBookingsAndComments() {
        Booking booking = new Booking(null,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item, booker, BookingStatus.APPROVED);
        bookingRepository.save(booking);

        Comment comment = new Comment(null, "Отличная дрель!", item, booker, LocalDateTime.now());
        commentRepository.save(comment);

        ItemWithBookingsDto result = itemService.getItemByIdWithBookings(item.getId(), owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getText()).isEqualTo("Отличная дрель!");
    }

    @Test
    void searchItems_shouldReturnAvailableItemsByText() {
        List<ItemDto> result = itemService.searchItems("дрель", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Дрель");
    }

    @Test
    void searchItems_withBlankText_shouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("", 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void updateItem_shouldUpdateOnlyProvidedFields() {
        ItemDto updateDto = new ItemDto(null, "Новое имя", null, null, null);

        ItemDto result = itemService.updateItem(item.getId(), updateDto, owner.getId());

        assertThat(result.getName()).isEqualTo("Новое имя");
        assertThat(result.getDescription()).isEqualTo("Мощная дрель");
        assertThat(result.getAvailable()).isEqualTo(true);
    }

    @Test
    void searchItems_withUnavailableItem_shouldNotReturnIt() {
        item.setAvailable(false);
        itemRepository.save(item);

        List<ItemDto> result = itemService.searchItems("дрель", 0, 10);

        assertThat(result).isEmpty();
    }
}

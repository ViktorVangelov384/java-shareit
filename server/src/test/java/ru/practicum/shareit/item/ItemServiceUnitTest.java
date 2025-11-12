package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceIml;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceIml itemService;

    @Test
    void updateItem_withNonOwner_shouldThrowAccessDeniedException() {
        User owner = new User(1L, "Owner", "owner@email.com");
        User otherUser = new User(2L, "Other", "other@email.com");
        Item existingItem = new Item(1L, "Дрель", "Описание", true, owner, null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));

        ItemDto updateDto = new ItemDto(null, "Новое имя", null, null, null);

        assertThatThrownBy(() -> itemService.updateItem(1L, updateDto, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Только владелец может обновлять вещь");

        verify(itemRepository, never()).save(any());
    }

    @Test
    void createItem_withNonExistentUser_shouldThrowNotFoundException() {
        ItemDto itemDto = new ItemDto(null, "Дрель", "Описание", true, null);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(itemDto, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    void searchItems_withBlankText_shouldReturnEmptyList() {
        var result = itemService.searchItems("   ", 0, 10);

        assertThat(result).isEmpty();
        verifyNoInteractions(itemRepository);
    }

    @Test
    void updateItem_withValidData_shouldUpdateItem() {
        User owner = new User(1L, "Owner", "owner@email.com");
        Item existingItem = new Item(1L, "Старое имя", "Старое описание", true, owner, null);
        ItemDto updateDto = new ItemDto(null, "Новое имя", "Новое описание", false, null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto result = itemService.updateItem(1L, updateDto, 1L);

        assertThat(result.getName()).isEqualTo("Новое имя");
        assertThat(result.getDescription()).isEqualTo("Новое описание");
        assertThat(result.getAvailable()).isEqualTo(false);
    }

    @Test
    void getAllItemsByOwner_withPagination_shouldReturnPaginatedResults() {
        User owner = new User(1L, "Owner", "owner@email.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        Item item1 = new Item(1L, "Item1", "Desc1", true, owner, null);
        Item item2 = new Item(2L, "Item2", "Desc2", true, owner, null);
        when(itemRepository.findByOwnerIdOrderById(eq(1L), any()))
                .thenReturn(List.of(item1, item2));

        when(bookingRepository.findLastBookingsForItems(any(), any(), any()))
                .thenReturn(List.of());
        when(bookingRepository.findNextBookingsForItems(any(), any(), any()))
                .thenReturn(List.of());
        when(commentRepository.findByItemIdInOrderByCreatedDesc(any()))
                .thenReturn(List.of());

        List<ItemWithBookingsDto> result = itemService.getAllItemsByOwner(1L, 0, 1);

        assertThat(result).hasSize(2);
    }

    @Test
    void getItemByIdWithBookings_withNoBookingsOrComments_shouldReturnBasicItem() {
        User owner = new User(1L, "Owner", "owner@email.com");
        Item item = new Item(1L, "Дрель", "Описание", true, owner, null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        lenient().when(bookingRepository.findLastBookingsForItems(any(List.class), any(), any()))
                .thenReturn(List.of());
        lenient().when(bookingRepository.findNextBookingsForItems(any(List.class), any(), any()))
                .thenReturn(List.of());
        lenient().when(commentRepository.findByItemIdInOrderByCreatedDesc(any(List.class)))
                .thenReturn(List.of());

        ItemWithBookingsDto result = itemService.getItemByIdWithBookings(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
        assertThat(result.getComments()).isEmpty();
    }

    @Test
    void searchItems_withPagination_shouldReturnPaginatedResults() {
        User owner = new User(1L, "Owner", "owner@email.com");
        Item item = new Item(1L, "Дрель", "Описание", true, owner, null);

        when(itemRepository.searchAvailableItems(eq("дрель"), any()))
                .thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItems("дрель", 0, 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Дрель");
    }


}

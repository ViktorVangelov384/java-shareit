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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceIml;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {

    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private CommentRepository commentRepository;

    @InjectMocks private ItemServiceIml itemService;

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
}

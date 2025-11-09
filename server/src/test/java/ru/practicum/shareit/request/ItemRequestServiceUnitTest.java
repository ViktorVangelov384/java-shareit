package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceIml;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceUnitTest {

    @Mock private ItemRequestRepository itemRequestRepository;
    @Mock private UserRepository userRepository;
    @Mock private ItemRepository itemRepository;

    @InjectMocks private ItemRequestServiceIml itemRequestService;

    @Test
    void createRequest_withNonExistentUser_shouldThrowNotFoundException() {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Нужна дрель", null, null, null);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.createRequest(requestDto, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    void createRequest_shouldCreateRequest() {
        User requestor = new User(1L, "Requestor", "requestor@email.com");
        ItemRequestDto requestDto = new ItemRequestDto(null, "Нужна дрель",
                null, null, null);
        ItemRequest savedRequest = new ItemRequest(1L, "Нужна дрель", requestor, LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(savedRequest);

        ItemRequestDto result = itemRequestService.createRequest(requestDto, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void getRequestById_withNonExistentRequest_shouldThrowNotFoundException() {
        User user = new User(1L, "User", "user@email.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getRequestById(999L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Запрос не найден");
    }

    @Test
    void getRequestById_withNonExistentUser_shouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getRequestById(1L, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    void getAllRequestsByRequestor_withNonExistentUser_shouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getAllRequestsByRequestor(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    void getAllRequests_withNonExistentUser_shouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getAllRequests(999L, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }
}

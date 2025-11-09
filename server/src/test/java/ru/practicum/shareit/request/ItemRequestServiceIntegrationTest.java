package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceIml;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ItemRequestServiceIml.class)
class ItemRequestServiceIntegrationTest {

    @Autowired private ItemRequestServiceIml itemRequestService;
    @Autowired private UserRepository userRepository;
    @Autowired private ItemRequestRepository itemRequestRepository;
    @Autowired private ItemRepository itemRepository;

    private User requestor;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requestor = userRepository.save(new User(null, "Requestor", "requestor@email.com"));
        otherUser = userRepository.save(new User(null, "Other", "other@email.com"));
    }

    @Test
    void createRequest_shouldSaveRequestToDatabase() {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Нужна дрель", null, null, null);

        ItemRequestDto result = itemRequestService.createRequest(requestDto, requestor.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
        assertThat(result.getRequestorId()).isEqualTo(requestor.getId());
        assertThat(result.getCreated()).isNotNull();

        ItemRequest savedRequest = itemRequestRepository.findById(result.getId()).orElse(null);
        assertThat(savedRequest).isNotNull();
        assertThat(savedRequest.getDescription()).isEqualTo("Нужна дрель");
    }

    @Test
    void getAllRequestsByRequestor_shouldReturnUserRequestsWithItems() {
        ItemRequest request = new ItemRequest(null, "Нужна дрель", requestor, LocalDateTime.now());
        itemRequestRepository.save(request);

        Item item = new Item(null, "Дрель", "Мощная", true, otherUser, request.getId());
        itemRepository.save(item);

        List<ItemRequestDto> result = itemRequestService.getAllRequestsByRequestor(requestor.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItems()).hasSize(1);
        assertThat(result.get(0).getItems().get(0).getName()).isEqualTo("Дрель");
    }

    @Test
    void getAllRequests_shouldReturnPaginatedOtherUsersRequests() {
        ItemRequest request1 = new ItemRequest(null, "Request 1", requestor, LocalDateTime.now().minusDays(1));
        ItemRequest request2 = new ItemRequest(null, "Request 2", otherUser, LocalDateTime.now());
        itemRequestRepository.saveAll(List.of(request1, request2));

        List<ItemRequestDto> result = itemRequestService.getAllRequests(requestor.getId(), 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Request 2");
    }
}

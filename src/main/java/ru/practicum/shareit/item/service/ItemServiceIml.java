package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceIml implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = ItemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Только владелец может обновлять вещь");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getRequestId() != null) {
            existingItem.setRequestId(itemDto.getRequestId());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemWithBookingsDto getItemByIdWithBookings(Long itemId, Long userId) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Продукт не найден"));

        ItemWithBookingsDto itemWithBookings = ItemMapper.toItemWithBookingsDto(item);

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            Optional<Booking> lastBooking =
                    bookingRepository.findLastBookingWithBooker(itemId, now, BookingStatus.APPROVED);
            Optional<Booking> nextBooking =
                    bookingRepository.findNextBookingWithBooker(itemId, now, BookingStatus.APPROVED);

            lastBooking.ifPresent(booking -> itemWithBookings.setLastBooking(
                    new ItemWithBookingsDto.BookingInfoDto(booking.getId(), booking.getBooker().getId())));

            nextBooking.ifPresent(booking -> itemWithBookings.setNextBooking(
                    new ItemWithBookingsDto.BookingInfoDto(booking.getId(), booking.getBooker().getId())));
        }

        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(toList());
        itemWithBookings.setComments(comments);

        return itemWithBookings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemWithBookingsDto> getAllItemsByOwner(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Item> items = itemRepository.findByOwnerIdOrderById(ownerId);

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();
        List<Long> itemIds = items.stream().map(Item::getId).collect(toList());

        Map<Long, Booking> lastBookingsMap = bookingRepository
                .findLastBookingsForItems(itemIds, now, BookingStatus.APPROVED)
                .stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking,
                        (existing, replacement) -> existing
                ));

        Map<Long, Booking> nextBookingsMap = bookingRepository
                .findNextBookingsForItems(itemIds, now, BookingStatus.APPROVED)
                .stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking,
                        (existing, replacement) -> existing
                ));

        Map<Long, List<Comment>> commentsMap = commentRepository.findByItemIdInOrderByCreatedDesc(itemIds)
                .stream()
                .collect(groupingBy(comment -> comment.getItem().getId(), toList()));

        return items.stream().map(item -> {
            ItemWithBookingsDto itemWithBookings = ItemMapper.toItemWithBookingsDto(item);

            Booking lastBooking = lastBookingsMap.get(item.getId());
            if (lastBooking != null) {
                itemWithBookings.setLastBooking(
                        new ItemWithBookingsDto.BookingInfoDto(lastBooking.getId(), lastBooking.getBooker().getId()));
            }


            Booking nextBooking = nextBookingsMap.get(item.getId());
            if (nextBooking != null) {
                itemWithBookings.setNextBooking(
                        new ItemWithBookingsDto.BookingInfoDto(nextBooking.getId(), nextBooking.getBooker().getId()));
            }

            List<CommentDto> commentDtos = commentsMap.getOrDefault(item.getId(), Collections.emptyList())
                    .stream()
                    .map(CommentMapper::toCommentDto)
                    .collect(toList());
            itemWithBookings.setComments(commentDtos);

            return itemWithBookings;
        }).collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.searchAvailableItems(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Booking> userBookings = bookingRepository.findByItemIdAndBookerIdAndEndBeforeAndStatus(
                itemId, userId, LocalDateTime.now(), BookingStatus.APPROVED);

        if (userBookings.isEmpty()) {
            throw new ValidationException("Пользователь не брал вещь в аренду или аренда еще не завершена");
        }

        Comment comment = CommentMapper.toCommentFromCreate(commentDto, item, author);
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(savedComment);
    }
}

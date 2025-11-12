package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start,
                                                          LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start,
                                                             LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    List<Booking> findByItemIdAndBookerIdAndEndBeforeAndStatus(
            Long itemId, Long bookerId, LocalDateTime now, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds AND " +
            "b.start < :now AND b.status = :status ORDER BY b.start DESC")
    List<Booking> findLastBookingsForItems(@Param("itemIds") List<Long> itemIds,
                                           @Param("now") LocalDateTime now,
                                           @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds AND " +
            "b.start > :now AND b.status = :status ORDER BY b.start ASC")
    List<Booking> findNextBookingsForItems(@Param("itemIds") List<Long> itemIds,
                                           @Param("now") LocalDateTime now,
                                           @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker WHERE b.item.id = :itemId AND" +
            " b.start < :now AND b.status = :status ORDER BY b.start DESC LIMIT 1")
    Optional<Booking> findLastBookingWithBooker(@Param("itemId") Long itemId,
                                                @Param("now") LocalDateTime now,
                                                @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker WHERE b.item.id = :itemId AND" +
            " b.start > :now AND b.status = :status ORDER BY b.start ASC LIMIT 1")
    Optional<Booking> findNextBookingWithBooker(@Param("itemId") Long itemId,
                                                @Param("now") LocalDateTime now,
                                                @Param("status") BookingStatus status);

}

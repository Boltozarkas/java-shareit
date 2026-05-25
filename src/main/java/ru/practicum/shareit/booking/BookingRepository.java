package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Существующие методы для booker
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    // Существующие методы для owner
    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    // Для комментариев
    @Query("select b from Booking b where b.item.id = :itemId and b.booker.id = :userId and b.status = 'APPROVED' and b.end < :now")
    List<Booking> findCompletedBookingsByItemAndUser(Long itemId, Long userId, LocalDateTime now);

    // Для одного предмета
    Optional<Booking> findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(Long itemId, LocalDateTime now, BookingStatus status);

    Optional<Booking> findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(Long itemId, LocalDateTime now, BookingStatus status);

    // Новые методы для пакетной загрузки
    @Query("select b from Booking b where b.item.id in :itemIds and b.status = :status")
    List<Booking> findByItemIdInAndStatus(List<Long> itemIds, BookingStatus status);

    List<Booking> findByItemIdInAndStatusAndEndBeforeOrderByEndDesc(List<Long> itemIds, BookingStatus status, LocalDateTime now);

    List<Booking> findByItemIdInAndStatusAndStartAfterOrderByStartAsc(List<Long> itemIds, BookingStatus status, LocalDateTime now);
}
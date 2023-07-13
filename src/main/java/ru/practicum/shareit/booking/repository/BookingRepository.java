package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.StatusEnum;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBooker_Id(Long bookerId);

    List<Booking> findByBookerIdAndItemIdAndStatusAndStartIsBefore(Long userId,
                                                                   Long itemId,
                                                                   StatusEnum status,
                                                                   LocalDateTime now);

    List<Booking> findAllByBooker_IdAndStatus(Long bookerId,
                                              StatusEnum statusEnum);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 ")
    List<Booking> findAllByItemOwner(Long ownerId);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = ?2 ")
    List<Booking> findAllByItemOwnerAndStatus(Long ownerId,
                                              StatusEnum statusEnum);

    @Query("select b from Booking b where (b.item.id = :itemId) and " +
            "(b.status = :status) and " +
            "(b.start between :start and :end " +
            "OR b.end between :start and :end " +
            "OR b.start <= :start AND b.end >= :end)")
    List<Booking> findBookingsAtSameTime(Long itemId, StatusEnum status, LocalDateTime start, LocalDateTime end);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and (b.status = ?2 " +
            "or b.status = ?3) " +
            "order by b.start desc ")
    List<Booking> findAllByItem_IdAndStatusOrderByStartDesc(Long itemId, StatusEnum status1, StatusEnum status2);

    List<Booking> findAllByBooker_IdAndEndBefore(Long userId, LocalDateTime now);

    List<Booking> findAllByBooker_IdAndStartAfter(Long userId, LocalDateTime now);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long userId, LocalDateTime now, LocalDateTime now1);

    List<Booking> findByItemOwnerIdAndEndIsBefore(Long ownerId, LocalDateTime now);

    List<Booking> findByItemOwnerIdAndStartIsAfter(Long ownerId, LocalDateTime now);

    List<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long ownerId, LocalDateTime now, LocalDateTime now1);
}

package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.StatusEnum;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findAllByBooker_Id(Long bookerId, Pageable page);

    List<Booking> findByBookerIdAndItemIdAndStatusAndStartIsBefore(Long userId,
                                                                   Long itemId,
                                                                   StatusEnum status,
                                                                   LocalDateTime now);

    Page<Booking> findAllByBooker_IdAndStatus(Long bookerId,
                                              StatusEnum statusEnum,
                                              Pageable page);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 ")
    Page<Booking> findAllByItemOwner(Long ownerId, Pageable page);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = ?2 ")
    Page<Booking> findAllByItemOwnerAndStatus(Long ownerId,
                                              StatusEnum statusEnum,
                                              Pageable page);

    @Query("select b from Booking b where (b.item.id = :itemId) and " +
            "(b.status = :status) and " +
            "(b.start between :start and :end " +
            "OR b.end between :start and :end " +
            "OR b.start <= :start AND b.end >= :end)")
    List<Booking> findBookingsAtSameTime(Long itemId,
                                         StatusEnum status,
                                         LocalDateTime start,
                                         LocalDateTime end);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and (b.status = ?2 " +
            "or b.status = ?3) " +
            "order by b.start desc ")
    List<Booking> findAllByItem_IdAndStatusOrderByStartDesc(Long itemId,
                                                            StatusEnum status1,
                                                            StatusEnum status2);

    Page<Booking> findAllByBooker_IdAndEndBefore(Long userId, LocalDateTime now, Pageable page);

    Page<Booking> findAllByBooker_IdAndStartAfter(Long userId, LocalDateTime now, Pageable page);

    Page<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long userId,
                                                              LocalDateTime now,
                                                              LocalDateTime now1,
                                                              Pageable page);

    Page<Booking> findByItemOwnerIdAndEndIsBefore(Long ownerId, LocalDateTime now, Pageable page);

    Page<Booking> findByItemOwnerIdAndStartIsAfter(Long ownerId, LocalDateTime now, Pageable page);

    Page<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long ownerId,
                                                                 LocalDateTime now,
                                                                 LocalDateTime now1,
                                                                 Pageable page);
}

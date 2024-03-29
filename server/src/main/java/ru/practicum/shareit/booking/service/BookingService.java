package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.util.List;

public interface BookingService {
    BookingOutDto findBooking(Long bookingId, Long userId);

    List<BookingOutDto> findAllForUser(Long userId, String state, Integer from, Integer size);

    List<BookingOutDto> findAllForOwner(Long ownerId, String state, Integer from, Integer size);

    BookingOutDto createBooking(BookingDto bookingDto, Long userId);

    BookingOutDto approveOrRejectBooking(Long ownerId, Boolean approved, Long bookingId);

}

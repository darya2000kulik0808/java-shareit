package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.enums.StateEnum;
import ru.practicum.shareit.enums.StatusEnum;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {

    private static final Sort SORTED = Sort.by(Sort.Direction.DESC, "start");
    private BookingRepository bookingRepository;
    private ItemRepository itemRepository;
    private UserRepository userRepository;

    @Override
    public BookingOutDto findBooking(Long bookingId, Long userId) {
        checkUser(userId);
        Booking booking = checkBooking(bookingId);
        if (booking.getBooker().getId().equals(userId) ||
                booking.getItem().getOwner().getId().equals(userId)) {
            return BookingMapper.toBookingOutDto(booking);
        } else {
            throw new BookingAccessDeniedForOwnerException("Вы не являетесь пользователем, забронировавшим вещь," +
                    " или владельцем вещи. Просмотр бронирования невозможен.");
        }
    }

    @Override
    public List<BookingOutDto> findAllForUser(Long userId, String state, Integer from, Integer size) {
        checkUser(userId);
        PageRequest page = PageRequest.of(from / size, size, SORTED);
        try {
            StateEnum stateEnum = StateEnum.valueOf(state);
            switch (stateEnum) {
                case WAITING:
                    return bookingRepository
                            .findAllByBooker_IdAndStatus(userId, StatusEnum.WAITING, page)
                            .stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .map(BookingMapper::toBookingOutDto)
                            .collect(Collectors.toList());
                case PAST:
                    return bookingRepository
                            .findAllByBooker_IdAndEndBefore(userId, LocalDateTime.now(), page)
                            .stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .map(BookingMapper::toBookingOutDto)
                            .collect(Collectors.toList());
                case FUTURE:
                    return bookingRepository
                            .findAllByBooker_IdAndStartAfter(userId, LocalDateTime.now(), page)
                            .stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .map(BookingMapper::toBookingOutDto)
                            .collect(Collectors.toList());
                case CURRENT:
                    return bookingRepository
                            .findByBookerIdAndStartIsBeforeAndEndIsAfter(userId,
                                    LocalDateTime.now(), LocalDateTime.now(), page)
                            .stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .map(BookingMapper::toBookingOutDto)
                            .collect(Collectors.toList());
                case REJECTED:
                    return bookingRepository
                            .findAllByBooker_IdAndStatus(userId, StatusEnum.REJECTED, page)
                            .stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .map(BookingMapper::toBookingOutDto)
                            .collect(Collectors.toList());
                default:
                    return bookingRepository
                            .findAllByBooker_Id(userId, page)
                            .getContent()
                            .stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .map(BookingMapper::toBookingOutDto)
                            .collect(Collectors.toList());
            }
        } catch (IllegalArgumentException e) {
            throw new UnknownStateException(String.format("Unknown state: %s", state));
        }
    }

    @Override
    public List<BookingOutDto> findAllForOwner(Long ownerId, String state, Integer from, Integer size) {
        checkUser(ownerId);
        PageRequest page = PageRequest.of(from / size, size, SORTED);
        try {
            StateEnum stateEnum = StateEnum.valueOf(state);
            switch (stateEnum) {
                case WAITING:
                    return bookingRepository
                            .findAllByItemOwnerAndStatus(ownerId, StatusEnum.WAITING, page)
                            .stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .map(BookingMapper::toBookingOutDto)
                            .collect(Collectors.toList());
                case PAST:
                    return bookingRepository
                            .findByItemOwnerIdAndEndIsBefore(ownerId, LocalDateTime.now(), page)
                            .stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .map(BookingMapper::toBookingOutDto)
                            .collect(Collectors.toList());
                case FUTURE:
                    return bookingRepository
                            .findByItemOwnerIdAndStartIsAfter(ownerId, LocalDateTime.now(), page)
                            .stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .map(BookingMapper::toBookingOutDto)
                            .collect(Collectors.toList());
                case CURRENT:
                    return bookingRepository
                            .findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(ownerId,
                                    LocalDateTime.now(), LocalDateTime.now(), page)
                            .stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .map(BookingMapper::toBookingOutDto)
                            .collect(Collectors.toList());
                case REJECTED:
                    return bookingRepository
                            .findAllByItemOwnerAndStatus(ownerId, StatusEnum.REJECTED, page)
                            .stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .map(BookingMapper::toBookingOutDto)
                            .collect(Collectors.toList());
                default:
                    return bookingRepository
                            .findAllByItemOwner(ownerId, page)
                            .getContent()
                            .stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .map(BookingMapper::toBookingOutDto)
                            .collect(Collectors.toList());
            }
        } catch (IllegalArgumentException e) {
            throw new UnknownStateException(String.format("Unknown state: %s", state));
        }
    }

    @Override
    public BookingOutDto createBooking(BookingDto bookingDto, Long userId) {
        User user = checkUser(userId);
        Item item = checkItem(bookingDto.getItemId());
        checkTime(bookingDto.getStart(), bookingDto.getEnd());
        if (item.getOwner().equals(user)) {
            throw new BookingAccessDeniedForOwnerException(String.format(
                    "Вещь с id %d  недоступна для бронирования для владельца", bookingDto.getItemId()));
        }
        if (!item.isAvailable()) {
            throw new BookingAccessDeniedException(String.format(
                    "Вещь с id %d  недоступна для бронирования", bookingDto.getItemId()));
        }
        List<Booking> bookingsAtSameTime = bookingRepository.findBookingsAtSameTime(bookingDto.getItemId(),
                StatusEnum.APPROVED, bookingDto.getStart(), bookingDto.getEnd());
        if (!bookingsAtSameTime.isEmpty()) {
            throw new ValidException("Время для аренды недоступно");
        }
        Booking booking = BookingMapper.toBooking(bookingDto, user, item);
        booking.setStatus(StatusEnum.WAITING);
        return BookingMapper.toBookingOutDto(bookingRepository.save(booking));
    }

    @Override
    public BookingOutDto approveOrRejectBooking(Long ownerId, Boolean approved, Long bookingId) {
        checkUser(ownerId);
        Booking booking = checkBooking(bookingId);
        checkOwner(ownerId, booking.getItem().getId());
        if (approved.equals(true)) {
            if (booking.getStatus().equals(StatusEnum.APPROVED)) {
                throw new ValidationException(String.format("Бронирование с id %d уже подтверждено", bookingId));
            }
            booking.setStatus(StatusEnum.APPROVED);
        } else if (approved.equals(false)) {
            if (booking.getStatus().equals(StatusEnum.REJECTED)) {
                throw new ValidationException(String.format("Бронирование с id %d уже отклонено", bookingId));
            }
            booking.setStatus(StatusEnum.REJECTED);
        }
        return BookingMapper.toBookingOutDto(bookingRepository.save(booking));
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }

    private Item checkItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Вещь с id %d не найдена", itemId)));
    }

    private Booking checkBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Бронирование с id %d не найдено", bookingId)));
    }

    private void checkOwner(Long userId, Long itemId) {
        checkUser(userId);
        Item item = checkItem(itemId);
        long ownerId = item.getOwner().getId();
        if (ownerId != userId) {
            throw new ObjectNotFoundException(
                    String.format("У пользователя с id %d нет вещи с id %d", userId, itemId));
        }
    }

    private void checkTime(LocalDateTime start, LocalDateTime end) {
        if (start.equals(end)) {
            throw new StartTimeAndEndTimeException("Время начала не может совпадать с концом!");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new StartTimeAndEndTimeException("Время начала не может быть в прошлом!");

        }
        if (end.isBefore(LocalDateTime.now())) {
            throw new StartTimeAndEndTimeException("Время конца не может быть в прошлом!");

        }
        if (start.isAfter(end)) {
            throw new StartTimeAndEndTimeException("Время начала не может быть позже конца!");

        }
    }
}

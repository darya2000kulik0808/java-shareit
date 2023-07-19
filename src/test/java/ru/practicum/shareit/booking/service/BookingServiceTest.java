package ru.practicum.shareit.booking.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.enums.StatusEnum;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "start");

    @Mock
    BookingRepository repository;

    @Mock
    private UserRepository userRepo;

    @Mock
    private ItemRepository itemRepo;

    @InjectMocks
    BookingServiceImpl service;

    private User owner;
    private User booker;

    private User user;
    private Item item;
    private Booking booking;
    private Booking booking2;

    @BeforeEach
    void setup() {
        LocalDateTime start = LocalDateTime.now().minusSeconds(120);
        LocalDateTime end = LocalDateTime.now().minusSeconds(60);
        owner = new User();
        owner.setName("name");
        owner.setEmail("e@mail.ru");
        owner.setId(1L);

        booker = new User();
        booker.setName("name2");
        booker.setEmail("e2@mail.ru");
        booker.setId(2L);

        user = new User();
        user.setName("name3");
        user.setEmail("e3@mail.ru");
        user.setId(3L);

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Хочешь, задрелю соседей, что мешают спать?");
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(StatusEnum.APPROVED);

        booking2 = new Booking();
        booking2.setId(2L);
        booking2.setStart(start.plusSeconds(10));
        booking2.setEnd(end.plusSeconds(10));
        booking2.setItem(item);
        booking2.setBooker(booker);
        booking2.setStatus(StatusEnum.APPROVED);
    }

    @Test
    void findById() {
        long userId = user.getId();
        long bookingId = booking.getId();
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findById(bookingId)).thenReturn(Optional.of(booking));
        String error = "Вы не являетесь пользователем, забронировавшим вещь, " +
                "или владельцем вещи. Просмотр бронирования невозможен.";
        BookingAccessDeniedForOwnerException exception = assertThrows(
                BookingAccessDeniedForOwnerException.class,
                () -> service.findBooking(bookingId, userId)
        );
        assertEquals(error, exception.getMessage());

        long ownerId = owner.getId();
        when(userRepo.findById(ownerId)).thenReturn(Optional.of(owner));
        when(repository.findById(bookingId)).thenReturn(Optional.of(booking));
        BookingOutDto bookingOutDto = service.findBooking(bookingId, ownerId);
        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());

        //Regular Case with booker
        long bookerId = owner.getId();
        when(userRepo.findById(bookerId)).thenReturn(Optional.of(booker));
        when(repository.findById(bookingId)).thenReturn(Optional.of(booking));
        bookingOutDto = service.findBooking(bookingId, bookerId);
        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
    }

    @Test
    void findAllForUser() {
        int from = 0;
        int size = 1;
        long userId = booker.getId();
        PageRequest page = PageRequest.of(0, size, SORT);
        when(userRepo.findById(userId)).thenReturn(Optional.of(booker));

        //Fail By Wrong State
        String error = "Unknown state: UNKNOWN";
        UnknownStateException exception = assertThrows(
                UnknownStateException.class,
                () -> service.findAllForUser(userId, "UNKNOWN", from, size)
        );
        assertEquals(error, exception.getMessage());

        //State All
        when(repository.findAllByBooker_Id(userId, page)).thenReturn(new PageImpl<>(List.of(booking)));
        List<BookingOutDto> bookingOutDtos = service.findAllForUser(userId, "ALL", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());

        //State PAST
        when(repository.findAllByBooker_IdAndEndBefore(anyLong(), any(), any())).thenReturn(new PageImpl<>(List.of(booking)));

        bookingOutDtos = service.findAllForUser(userId, "PAST", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        //State CURRENT
        booking.setEnd(LocalDateTime.now().plusSeconds(120));

        when(repository.findByBookerIdAndStartIsBeforeAndEndIsAfter(anyLong(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(booking)));

        bookingOutDtos = service.findAllForUser(userId, "CURRENT", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        //State FUTURE
        booking.setStart(LocalDateTime.now().plusSeconds(60));

        when(repository.findAllByBooker_IdAndStartAfter(anyLong(), any(), any())).thenReturn(new PageImpl<>(List.of(booking)));

        bookingOutDtos = service.findAllForUser(userId, "FUTURE", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        //STATE WAITING
        booking.setStatus(StatusEnum.WAITING);

        when(repository.findAllByBooker_IdAndStatus(anyLong(), any(), any())).thenReturn(new PageImpl<>(List.of(booking)));

        bookingOutDtos = service.findAllForUser(userId, "WAITING", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        //SATE REJECTING
        booking.setStatus(StatusEnum.REJECTED);

        bookingOutDtos = service.findAllForUser(userId, "REJECTED", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
    }

    @Test
    void findByOwnerItemsAndState() {
        int from = 0;
        int size = 1;
        long userId = owner.getId();
        PageRequest page = PageRequest.of(0, size, SORT);
        when(userRepo.findById(userId)).thenReturn(Optional.of(owner));

        //Fail By Wrong State
        String error = "Unknown state: UNKNOWN";
        UnknownStateException exception = assertThrows(
                UnknownStateException.class,
                () -> service.findAllForOwner(userId, "UNKNOWN", from, size)
        );
        assertEquals(error, exception.getMessage());

        //State ALL
        when(repository.findAllByItemOwner(userId, page)).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingOutDto> bookingOutDtos = service.findAllForOwner(userId, "ALL", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());

        //State PAST
        when(repository.findByItemOwnerIdAndEndIsBefore(anyLong(), any(), any())).thenReturn(new PageImpl<>(List.of(booking)));

        bookingOutDtos = service.findAllForOwner(userId, "PAST", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        //STATE CURRENT
        booking.setEnd(LocalDateTime.now().plusSeconds(120));
        when(repository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(anyLong(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));

        bookingOutDtos = service.findAllForOwner(userId, "CURRENT", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        //State FUTURE
        booking.setStart(LocalDateTime.now().plusSeconds(60));
        when(repository.findByItemOwnerIdAndStartIsAfter(anyLong(), any(), any())).thenReturn(new PageImpl<>(List.of(booking)));

        bookingOutDtos = service.findAllForOwner(userId, "FUTURE", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        //State WAITING
        booking.setStatus(StatusEnum.WAITING);
        when(repository.findAllByItemOwnerAndStatus(anyLong(), any(), any())).thenReturn(new PageImpl<>(List.of(booking)));

        bookingOutDtos = service.findAllForOwner(userId, "WAITING", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        //State REJECT
        booking.setStatus(StatusEnum.REJECTED);
        when(repository.findAllByItemOwnerAndStatus(anyLong(), any(), any())).thenReturn(new PageImpl<>(List.of(booking)));

        bookingOutDtos = service.findAllForOwner(userId, "REJECTED", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
    }

    @Test
    void add() {
        long ownerId = owner.getId();
        long itemId = item.getId();

        LocalDateTime start = booking.getStart().plusSeconds(300);
        LocalDateTime end = booking.getEnd().plusSeconds(600);

        BookingDto bookingWithSameTime = new BookingDto();
        bookingWithSameTime.setId(1L);
        bookingWithSameTime.setStart(start);
        bookingWithSameTime.setEnd(start);
        bookingWithSameTime.setItemId(item.getId());
        bookingWithSameTime.setBookerId(booker.getId());
        bookingWithSameTime.setStatus(StatusEnum.APPROVED);

        BookingDto bookingToSave = BookingDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        when(userRepo.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        String error = String.format("Вещь с id %d  недоступна для бронирования для владельца", itemId);
        BookingAccessDeniedForOwnerException exception = assertThrows(
                BookingAccessDeniedForOwnerException.class,
                () -> service.createBooking(bookingToSave, ownerId));
        assertEquals(error, exception.getMessage());

        item.setAvailable(true);
        long bookerId = booker.getId();
        when(userRepo.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        error = "Время начала не может совпадать с концом!";
        StartTimeAndEndTimeException exception1 = assertThrows(
                StartTimeAndEndTimeException.class,
                () -> service.createBooking(bookingWithSameTime, bookerId));
        assertEquals(error, exception1.getMessage());

        item.setAvailable(false);
        when(userRepo.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        error = String.format("Вещь с id %d  недоступна для бронирования", itemId);
        BookingAccessDeniedException ex = assertThrows(
                BookingAccessDeniedException.class,
                () -> service.createBooking(bookingToSave, bookerId));
        assertEquals(error, ex.getMessage());

        item.setAvailable(true);
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(repository.findBookingsAtSameTime(itemId, StatusEnum.APPROVED, start, end))
                .thenReturn(List.of(booking2));
        error = "Время для аренды недоступно";
        ValidException exception2 = assertThrows(
                ValidException.class,
                () -> service.createBooking(bookingToSave, bookerId));
        assertEquals(error, exception2.getMessage());

        when(repository.findBookingsAtSameTime(itemId, StatusEnum.APPROVED, bookingToSave.getStart(), bookingToSave.getEnd()))
                .thenReturn(Collections.emptyList());
        when(repository.save(any())).thenReturn(booking);
        BookingOutDto bookingOutDto = service.createBooking(bookingToSave, bookerId);

        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
    }

    @Test
    void patch() {
        long userId = owner.getId();
        long bookingId = booking.getId();
        long itemId = item.getId();

        when(userRepo.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(repository.findById(bookingId)).thenReturn(Optional.of(booking));

        String error = String.format("Бронирование с id %d уже подтверждено", bookingId);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> service.approveOrRejectBooking(userId, true, bookingId)
        );
        assertEquals(error, exception.getMessage());

        //Regular Case
        when(repository.save(any())).thenReturn(booking);

        BookingOutDto bookingOutDto = service.approveOrRejectBooking(userId, false, bookingId);
        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
    }
}

package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.validation.ValidationGroups.Create;
import static ru.practicum.shareit.validation.ValidationGroups.Update;

/**
 * TODO Sprint add-bookings.
 */

@RestController
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(@Qualifier("BookingServiceImpl") BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/{bookingId}")
    public BookingOutDto findBookingsForUserOrOwner(@PathVariable Long bookingId,
                                                    @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.findBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingOutDto> findAllBookingsForOneUser(@RequestParam(defaultValue = "ALL") String state,
                                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.findAllForUser(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingOutDto> findAllForOwner(@RequestParam(defaultValue = "ALL") String state,
                                               @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return bookingService.findAllForOwner(ownerId, state);
    }

    @PostMapping
    @Validated(Create.class)
    public BookingOutDto createBooking(@Valid @RequestBody BookingDto bookingDto,
                                       @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    @Validated(Update.class)
    public BookingOutDto approveOrRejectBooking(@PathVariable Long bookingId,
                                                @RequestParam Boolean approved,
                                                @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return bookingService.approveOrRejectBooking(ownerId, approved, bookingId);
    }
}

package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/{bookingId}")
    public BookingOutDto findBookingsForUserOrOwner(@PathVariable Long bookingId,
                                                    @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.findBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingOutDto> findAllBookingsForOneUser(@RequestParam(defaultValue = "ALL") String state,
                                                         @RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @RequestParam(defaultValue = "0") Integer from,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        return bookingService.findAllForUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingOutDto> findAllForOwner(@RequestParam(defaultValue = "ALL") String state,
                                               @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return bookingService.findAllForOwner(ownerId, state.toUpperCase(), from, size);
    }

    @PostMapping
    public BookingOutDto createBooking(@RequestBody BookingDto bookingDto,
                                       @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingOutDto approveOrRejectBooking(@PathVariable Long bookingId,
                                                @RequestParam Boolean approved,
                                                @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return bookingService.approveOrRejectBooking(ownerId, approved, bookingId);
    }
}

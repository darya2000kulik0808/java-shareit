package ru.practicum.shareit.booking;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingClient bookingClient;

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> findBookingsForUserOrOwner(@PathVariable Long bookingId,
                                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingClient.findBooking(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> findAllBookingsForOneUser(@RequestParam(defaultValue = "ALL") String state,
                                                            @RequestHeader("X-Sharer-User-Id") Long userId,
                                                            @Min(value = 0,
                                                                    message = "Индекс первого элемента не может быть отрицательным!")
                                                            @RequestParam(defaultValue = "0") Integer from,
                                                            @Positive(
                                                                    message = "Количество элементов должно быть положительным!")
                                                            @RequestParam(defaultValue = "10") Integer size) {
        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        return bookingClient.findAllForUser(userId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findAllForOwner(@RequestParam(defaultValue = "ALL") String state,
                                                  @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                  @Min(value = 0,
                                                          message = "Индекс первого элемента не может быть отрицательным!")
                                                  @RequestParam(defaultValue = "0") Integer from,
                                                  @Positive(
                                                          message = "Количество элементов должно быть положительным!")
                                                  @RequestParam(defaultValue = "10") Integer size) {
        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        return bookingClient.findAllForOwner(ownerId, bookingState, from, size);
    }

    @PostMapping
    @Validated(ValidationGroups.Create.class)
    public ResponseEntity<Object> createBooking(@Valid @RequestBody BookItemRequestDto bookingDto,
                                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        if (!bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            throw new ValidationException("Дата окончания бронирования должна быть позже даты начала");
        }
        return bookingClient.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    @Validated(ValidationGroups.Update.class)
    public ResponseEntity<Object> approveOrRejectBooking(@PathVariable Long bookingId,
                                                         @RequestParam Boolean approved,
                                                         @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return bookingClient.approveOrRejectBooking(ownerId, approved, bookingId);
    }
}

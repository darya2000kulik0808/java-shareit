package ru.practicum.shareit.exceptions;

public class BookingAccessDeniedForOwnerException extends RuntimeException {

    public BookingAccessDeniedForOwnerException(String message) {
        super(message);
    }
}

package ru.practicum.shareit.exceptions;

public class CommentAccessDeniedException extends RuntimeException {

    public CommentAccessDeniedException(String message) {
        super(message);
    }
}

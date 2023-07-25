package ru.practicum.shareit.validation;

import javax.validation.ValidationException;

public class CheckBlank {
    public static void checkNotBlank(String s, String parameterName) {
        if (s.isBlank()) {
            throw new ValidationException(String.format("%s не может быть пустым", parameterName));
        }
    }
}

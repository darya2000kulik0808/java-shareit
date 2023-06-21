package ru.practicum.shareit.enums;

public enum StatusEnum {
    WAITING, //новое бронирование, ожидает одобрения
    APPROVED, //бронирование подтверждено владельцем
    REJECTED, //бронирование отклонено владельцем
    CANCELED //бронирование отменено создателем
}

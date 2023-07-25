package ru.practicum.shareit.enums;

public enum StateEnum { //для эндпоинта по выдаче бронирований пользователя
    ALL, //все
    CURRENT, //текущие
    PAST, //завершённые
    FUTURE, //будущие
    WAITING, //ожидающие подтверждения
    REJECTED //отклонённые
}

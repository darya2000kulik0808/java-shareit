package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.enums.StatusEnum;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingOutDto {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private StatusEnum status;
    private ItemOutDto item;
    private UserDto booker;
}

package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;


/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
public class ItemOutDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private UserDto owner;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private List<CommentDto> comments;
    private Long requestId;
}

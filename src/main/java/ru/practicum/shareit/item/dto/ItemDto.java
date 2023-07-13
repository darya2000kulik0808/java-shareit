package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

import static ru.practicum.shareit.validation.ValidationGroups.Create;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
public class ItemDto {
    private Long id;
    @NotBlank(groups = Create.class, message = "Имя не может быть пустым.")
    private String name;
    @NotBlank(groups = Create.class, message = "Описание не может быть пустым.")
    private String description;
    @NotNull(groups = Create.class, message = "Укажите доступность товара")
    private Boolean available;
    private User owner;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private List<CommentDto> comments;
    private ItemRequest request;
}

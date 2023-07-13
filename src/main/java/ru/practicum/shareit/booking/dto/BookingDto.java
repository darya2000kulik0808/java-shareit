package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.enums.StatusEnum;
import ru.practicum.shareit.validation.ValidationGroups.Create;
import ru.practicum.shareit.validation.ValidationGroups.Update;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */

@Data
@Builder
public class BookingDto {
    private long id;
    @NotNull(groups = Create.class, message = "Время начала не может быть пустым.")
    private LocalDateTime start;
    @NotNull(groups = Create.class, message = "Время конца не может быть пустым.")
    private LocalDateTime end;
    @NotNull(groups = Update.class, message = "Статус не может быть пустым")
    private StatusEnum status;
    @NotNull(groups = Create.class, message = "Нужно указать, какую вещь вы бронируете. ")
    private Long itemId;
    private Long bookerId;
}

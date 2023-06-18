package ru.practicum.shareit.request.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */

@Data
@Builder
public class ItemRequest {
    private final long id;
    @NotBlank
    private final String description;
    @NotNull
    private final User requester;
    @NotNull
    private final LocalDateTime created;
}

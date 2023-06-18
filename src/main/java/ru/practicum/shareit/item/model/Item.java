package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

/**
 * TODO Sprint add-controllers.
 */

@Builder
@Data
public class Item {
    private long id;
    private String name;
    private String description;
    private boolean available;
    private User owner;
    private ItemRequest request;
}

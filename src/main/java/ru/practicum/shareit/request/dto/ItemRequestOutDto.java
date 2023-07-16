package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemOutDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */

@Data
@Builder
public class ItemRequestOutDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemOutDto> items;
}

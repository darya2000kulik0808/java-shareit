package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class ItemInDto {
    private Long id;
    @NotBlank(groups = ValidationGroups.Create.class, message = "Имя не может быть пустым.")
    private String name;
    @NotBlank(groups = ValidationGroups.Create.class, message = "Описание не может быть пустым.")
    private String description;
    @NotNull(groups = ValidationGroups.Create.class, message = "Укажите доступность товара")
    private Boolean available;
    private Long requestId;
}

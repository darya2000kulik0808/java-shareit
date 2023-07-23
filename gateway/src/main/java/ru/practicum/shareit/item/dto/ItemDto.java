package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class ItemDto {
    private Long id;
    @NotBlank(groups = ValidationGroups.Create.class, message = "Название не может быть пустым")
    @Size(max = 100, message = "Длина названия должна быть не более 100 символов")
    private String name;
    @NotBlank(groups = ValidationGroups.Create.class, message = "Описание не может быть пустым")
    @Size(max = 500, message = "Длина описания должна быть не более 500 символов")
    private String description;
    @NotNull(groups = ValidationGroups.Create.class, message = "Поле доступности вещи не может быть пустым")
    private Boolean available;
    private Long requestId;
}

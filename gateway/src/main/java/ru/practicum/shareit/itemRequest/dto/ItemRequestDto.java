package ru.practicum.shareit.itemRequest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class ItemRequestDto {
    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 250, message = "Длина описания должна до 250 символов")
    private String description;
}

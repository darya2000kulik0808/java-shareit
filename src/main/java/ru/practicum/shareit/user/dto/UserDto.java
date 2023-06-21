package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import static ru.practicum.shareit.validation.ValidationGroups.Create;

@Data
@Builder
public class UserDto {
    private Long id;
    @NotBlank(groups = Create.class, message = "Имя не может быть пустым.")
    private String name;
    @NotBlank(groups = Create.class, message = "Заполните поле электронной почты.")
    @Email(message = "Текст не является эл. почтой.")
    private String email;
}

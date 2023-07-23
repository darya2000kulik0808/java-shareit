package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    Long id;
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 500, message = "Текст комментария не может быть больше 500 символов")
    String text;
    String authorName;
    LocalDateTime created;
}

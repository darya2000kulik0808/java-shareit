package ru.practicum.shareit.item.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.item.dto.ItemOutDto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

import static ru.practicum.shareit.validation.ValidationGroups.Create;

@Data
@Builder
public class CommentDto {
    private long id;
    @NotBlank(groups = Create.class, message = "Текст комментария не может быть пустым")
    private String text;
    private ItemOutDto item;
    private String authorName;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime created;
}

package ru.practicum.shareit.item.comment.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemOutDto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

import static ru.practicum.shareit.validation.ValidationGroups.Create;

@Data
@Builder
public class CommentDto {
    private long id;
    @NotBlank(groups = Create.class)
    private String text;
    private ItemOutDto item;
    private String authorName;
    private LocalDateTime created;
}

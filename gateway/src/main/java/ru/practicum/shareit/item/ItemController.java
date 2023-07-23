package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.CheckBlank;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

import static ru.practicum.shareit.validation.ValidationGroups.Create;
import static ru.practicum.shareit.validation.ValidationGroups.Update;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    @Validated(Create.class)
    public ResponseEntity<Object> createItem(@Valid @RequestBody ItemDto itemDto,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.createItem(itemDto, userId);
    }

    @PostMapping("/{itemId}/comment")
    @Validated(Create.class)
    public ResponseEntity<Object> createComment(@Valid @RequestBody CommentDto commentDto,
                                                @RequestHeader("X-Sharer-User-Id") Long userId,
                                                @PathVariable Long itemId) {
        return itemClient.createComment(commentDto, userId, itemId);
    }

    @PatchMapping("/{itemId}")
    @Validated(Update.class)
    public ResponseEntity<Object> editItem(@Valid @RequestBody ItemDto itemDto,
                                           @PathVariable Long itemId,
                                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        if (itemDto.getName() != null) {
            CheckBlank.checkNotBlank(itemDto.getName(), "Название");
        }
        if (itemDto.getDescription() != null) {
            CheckBlank.checkNotBlank(itemDto.getDescription(), "Описание");
        }
        return itemClient.updateItem(itemDto, itemId, userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemById(@PathVariable Long id,
                                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.getItemById(id, userId);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @Min(value = 0,
                                                          message = "Индекс первого элемента не может быть отрицательным!")
                                                  @RequestParam(defaultValue = "0") Integer from,
                                                  @Positive(
                                                          message = "Количество элементов должно быть положительным!")
                                                  @RequestParam(defaultValue = "10") Integer size) {
        return itemClient.getAllByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findByText(@RequestParam String text,
                                             @RequestHeader("X-Sharer-User-Id") long userId,
                                             @Min(value = 0,
                                                     message = "Индекс первого элемента не может быть отрицательным!")
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @Positive(
                                                     message = "Количество элементов должно быть положительным!")
                                             @RequestParam(defaultValue = "10") Integer size) {
        return itemClient.getByText(userId, text, from, size);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable long itemId) {
        return itemClient.deleteItem(userId, itemId);
    }
}

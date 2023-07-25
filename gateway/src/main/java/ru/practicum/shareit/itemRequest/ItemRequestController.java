package ru.practicum.shareit.itemRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.itemRequest.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @GetMapping
    public ResponseEntity<Object> getUsersRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getAllUsersRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @Min(value = 0,
                                                         message = "Индекс первого элемента не может быть отрицательным!")
                                                 @RequestParam(defaultValue = "0") Integer from,
                                                 @Positive(
                                                         message = "Количество элементов должно быть положительным!")
                                                 @RequestParam(defaultValue = "10") Integer size) {
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getOneRequest(@PathVariable Long requestId,
                                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getOneRequest(requestId, userId);
    }

    @PostMapping
    public ResponseEntity<Object> createRequest(@Valid @RequestBody ItemRequestDto itemRequestInDto,
                                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.createRequest(userId, itemRequestInDto);
    }
}
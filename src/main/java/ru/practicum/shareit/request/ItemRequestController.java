package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @GetMapping
    public List<ItemRequestOutDto> getUsersRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllUsersRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestOutDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @Min(value = 0,
                                                          message = "Индекс первого элемента не может быть отрицательным!")
                                                  @RequestParam(defaultValue = "0") Integer from,
                                                  @Positive(
                                                          message = "Количество элементов должно быть положительным!")
                                                  @RequestParam(defaultValue = "10") Integer size) {
        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestOutDto getOneRequest(@PathVariable Long requestId,
                                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getOneRequest(requestId, userId);
    }

    @PostMapping
    @Validated(ValidationGroups.Create.class)
    public ItemRequestOutCreatedDto createRequest(@Valid @RequestBody ItemRequestInDto itemRequestInDto,
                                                  @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.createRequest(userId, itemRequestInDto);
    }
}

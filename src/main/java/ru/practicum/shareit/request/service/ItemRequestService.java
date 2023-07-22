package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;

import java.util.List;

public interface ItemRequestService {
    List<ItemRequestOutDto> getAllUsersRequests(Long userId);

    List<ItemRequestOutDto> getAllRequests(Long userId, Integer from, Integer size);

    ItemRequestOutDto getOneRequest(Long requestId, Long userId);

    ItemRequestOutCreatedDto createRequest(Long userId, ItemRequestInDto itemRequestInDto);
}

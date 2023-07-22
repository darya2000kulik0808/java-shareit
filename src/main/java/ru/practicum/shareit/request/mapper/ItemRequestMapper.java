package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestInDto itemRequestInDto, User requester, LocalDateTime created) {
        return ItemRequest.builder()
                .description(itemRequestInDto.getDescription())
                .requester(requester)
                .created(created)
                .build();
    }


    public static ItemRequest toItemRequest(ItemRequestOutDto itemRequestOutDto) {
        return ItemRequest.builder()
                .description(itemRequestOutDto.getDescription())
                .requester(itemRequestOutDto.getRequester())
                .created(itemRequestOutDto.getCreated())
                .build();
    }

    public static ItemRequestOutDto toItemRequestOutDto(ItemRequest itemRequest) {
        return ItemRequestOutDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                // .requester(itemRequest.getRequester())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequestOutCreatedDto toItemRequestOutCreatedDto(ItemRequest itemRequest) {
        return ItemRequestOutCreatedDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                // .requester(itemRequest.getRequester())
                .created(itemRequest.getCreated())
                .build();
    }
}

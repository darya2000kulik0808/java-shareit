package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemInDto;
import ru.practicum.shareit.item.dto.ItemOutDto;

import java.util.Collection;

public interface ItemService {

    ItemOutDto createItem(ItemInDto itemDto, Long userId);

    ItemOutDto updateItem(ItemInDto itemDto, Long id, Long userId);

    ItemOutDto getItemById(Long id, Long userId);

    void deleteItem(Long userId, Long itemId);

    Collection<ItemOutDto> getAllByUserId(Long userId);

    Collection<ItemOutDto> getByText(String text);

    CommentDto createComment(CommentDto commentDto, Long userId, Long itemId);
}

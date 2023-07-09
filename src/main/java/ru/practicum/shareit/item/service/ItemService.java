package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {

    ItemDto createItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(ItemDto itemDto, Long id, Long userId);

    ItemDto getItemById(Long id, Long userId);

    void deleteItem(Long userId, Long itemId);

    Collection<ItemDto> getAllByUserId(Long userId);

    Collection<ItemDto> getByText(String text);

    CommentDto createComment(CommentDto commentDto, Long userId, Long itemId);
}

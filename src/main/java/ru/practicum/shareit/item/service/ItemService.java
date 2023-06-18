package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {

    ItemDto createItem(ItemDto itemDto, long userId);

    ItemDto updateItem(ItemDto itemDto, long id, long userId);

    ItemDto getItemById(long id);

    void deleteItem(long userId, long itemId);

    Collection<ItemDto> getAllByUserId(long userId);

    Collection<ItemDto> getByText(String text);
}

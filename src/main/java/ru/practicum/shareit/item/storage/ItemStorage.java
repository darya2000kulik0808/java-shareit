package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemStorage {
    Item createItem(Item item);

    Item updateItem(Item item);

    void deleteItem(Long userId, Long itemId);

    Item getItemById(Long itemId);

    Collection<Item> getAllByUserId(Long userId);

    Collection<Item> getByText(String text);
}

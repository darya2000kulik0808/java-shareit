package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemStorage {
    Item createItem(Item item);

    Item updateItem(Item item);

    void deleteItem(long userId, long itemId);

    Item getItemById(long itemId);

    Collection<Item> getAllByUserId(long userId);

    Collection<Item> getByText(String text);
}

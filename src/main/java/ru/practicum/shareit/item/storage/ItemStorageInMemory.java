package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ItemStorageInMemory implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Long>> userToItemIds = new HashMap<>();
    private long id = 1;

    @Override
    public Item createItem(Item item) {
        item.setId(id);
        items.put(id, item);
        userToItemIds.computeIfAbsent(item.getOwner().getId(), k -> new ArrayList<>()).add(item.getId());
        id++;
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        if (items.containsKey(item.getId())) {
            items.put(item.getId(), item);
            return item;
        }
        throw new ObjectNotFoundException(String.format("Вещь с айди %d не найдена.", item.getId()));
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        userToItemIds.get(userId).remove(itemId);
        items.remove(itemId);
    }

    @Override
    public Item getItemById(Long itemId) {
        if (items.containsKey(itemId)) {
            return items.get(itemId);
        }
        throw new ObjectNotFoundException(String.format("Вещь с id %d не найдена", itemId));
    }

    @Override
    public Collection<Item> getAllByUserId(Long userId) {
        List<Long> itemsId = userToItemIds.getOrDefault(userId, new ArrayList<>());
        return itemsId.stream()
                .map(items::get)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> getByText(String text) {
        return items.values().stream()
                .filter(item -> item.isAvailable()
                        && (item.getName().toLowerCase().contains(text)
                        || item.getDescription().toLowerCase().contains(text)))
                .collect(Collectors.toList());
    }
}

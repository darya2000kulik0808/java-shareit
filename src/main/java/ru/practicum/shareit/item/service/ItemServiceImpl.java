package ru.practicum.shareit.item.service;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.validation.ValidationException;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@Qualifier("ItemServiceImpl")
public class ItemServiceImpl implements ItemService {

    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    public ItemServiceImpl(UserStorage userStorage, ItemStorage itemStorage) {
        this.userStorage = userStorage;
        this.itemStorage = itemStorage;
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User user = userStorage.getUserById(userId);
        Item item = ItemMapper.toItem(itemDto, user);
        return ItemMapper.toItemDto(itemStorage.createItem(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long id, Long userId) {

        Item item = checkOwner(userId, id);
        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                throw new ValidationException("Название не может быть пустым");
            }
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                throw new ValidationException("Описание не может быть пустым");
            }
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemStorage.updateItem(item));
    }

    @Override
    public ItemDto getItemById(Long id) {
        return ItemMapper.toItemDto(itemStorage.getItemById(id));
    }


    @Override
    public void deleteItem(Long userId, Long itemId) {
        checkOwner(userId, itemId);
        itemStorage.deleteItem(userId, itemId);
    }

    @Override
    public Collection<ItemDto> getAllByUserId(Long userId) {
        userStorage.getUserById(userId);
        return itemStorage.getAllByUserId(userId).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> getByText(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return itemStorage.getByText(text.toLowerCase()).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    private Item checkOwner(Long userId, Long itemId) {
        userStorage.getUserById(userId);
        Item item = itemStorage.getItemById(itemId);
        long ownerId = item.getOwner().getId();
        if (ownerId != userId) {
            throw new ObjectNotFoundException(
                    String.format("У пользователя с id %d нет вещи с id %d", userId, itemId));
        }
        return item;
    }
}

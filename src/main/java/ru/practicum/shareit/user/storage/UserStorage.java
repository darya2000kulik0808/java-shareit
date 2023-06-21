package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserStorage {
    User createUser(UserDto user);

    User updateUser(User user);

    boolean deleteUser(Long id);

    Collection<User> getAllUsers();

    User getUserById(Long userId);

    boolean checkEmail(String email);
}

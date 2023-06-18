package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();

    UserDto getUserById(long id);

    UserDto createUser(UserDto userDto);

    User updateUser(User user);

    boolean deleteUser(long id);

    UserDto patchUser(long id, UserDto userDto);
}

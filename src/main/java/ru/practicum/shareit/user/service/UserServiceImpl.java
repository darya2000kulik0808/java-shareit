package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ObjectAlreadyExistsException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.validation.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Qualifier("UserServiceImpl")
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userStorage.getAllUsers()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        return UserMapper.toUserDto(userStorage.getUserById(id));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (!userStorage.checkEmail(userDto.getEmail())) {
            return UserMapper.toUserDto(userStorage.createUser(userDto));
        } else {
            throw new ObjectAlreadyExistsException(
                    String.format("Пользователь с email %s уже существует", userDto.getEmail()));
        }
    }

    @Override
    public User updateUser(User user) {
        userStorage.updateUser(user);
        return user;
    }

    @Override
    public boolean deleteUser(Long id) {
        return userStorage.deleteUser(id);
    }

    @Override
    public UserDto patchUser(Long id, UserDto userDto) {
        User userToPatch = userStorage.getUserById(id);

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            userToPatch.setName(userDto.getName());
        }
        if (userDto.getName() != null) {
            if (userDto.getName().isBlank()) {
                throw new ValidationException("Имя не может быть пустым");
            }
            userToPatch.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (userDto.getEmail().isBlank()) {
                throw new ValidationException("E-mail не может быть пустым");
            }
            String oldEmail = userToPatch.getEmail();
            String email = userDto.getEmail();
            if (!email.equals(oldEmail) && userStorage.checkEmail(email)) {
                throw new ObjectAlreadyExistsException(String.format("Пользователь с email %s уже существует", email));
            }
            userToPatch.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(updateUser(userToPatch));
    }
}

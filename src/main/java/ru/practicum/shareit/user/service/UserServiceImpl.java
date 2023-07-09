package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ObjectAlreadyExistsException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Qualifier("UserServiceImpl")
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            return UserMapper.toUserDto(userOptional.get());
        } else {
            throw new ObjectNotFoundException(String.format("Пользователь с айди %d не найден.", id));
        }
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        try {
            return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
        } catch (ConstraintViolationException e) {
            throw new ObjectAlreadyExistsException(
                    String.format("Пользователь с email %s уже существует", userDto.getEmail()));
        }
    }

    @Override
    public User updateUser(User user) {
        userRepository.save(user);
        return user;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDto patchUser(Long id, UserDto userDto) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User userToPatch = userOptional.get();

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
                Optional<User> userWithEmail = Optional.ofNullable(userRepository.findUserByEmail(email));
                if (!email.equals(oldEmail) && userWithEmail.isPresent()) {
                    throw new ObjectAlreadyExistsException(String.format("Пользователь с email %s уже существует", email));
                }
                userToPatch.setEmail(userDto.getEmail());
            }
            return UserMapper.toUserDto(updateUser(userToPatch));
        } else {
            throw new ObjectNotFoundException(String.format("Пользователь с айди %d не найден.", id));
        }
    }
}

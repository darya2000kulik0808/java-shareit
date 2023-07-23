package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ObjectAlreadyExistsException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setName("name");
        user.setEmail("e@mail.ru");
        user.setId(1L);
    }

    @Test
    void findAll() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<UserDto> users = userService.getAllUsers();
        assertNotNull(users);
        assertEquals(0, users.size());

        when(userRepository.findAll()).thenReturn(List.of(user));
        users = userService.getAllUsers();
        assertNotNull(users);
        assertEquals(1, users.size());
    }

    @Test
    void findById() {
        long userIdNotFound = 0L;
        String error = String.format("Пользователь с айди %d не найден.", userIdNotFound);
        when(userRepository.findById(userIdNotFound)).thenReturn(Optional.empty());
        ObjectNotFoundException exception = assertThrows(
                ObjectNotFoundException.class,
                () -> userService.getUserById(userIdNotFound));
        assertEquals(error, exception.getMessage());

        long userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserDto userFound = userService.getUserById(userId);
        assertNotNull(userFound);
        assertEquals(userId, userFound.getId());
    }

    @Test
    void add() {
        User userToSave = new User();
        userToSave.setName("name");
        userToSave.setEmail("e@mail.ru");

        when(userRepository.save(any())).thenReturn(user);
        UserDto userDto = UserMapper.toUserDto(userToSave);
        UserDto userSaved = userService.createUser(userDto);

        assertNotNull(userSaved);
        assertEquals(user.getId(), userSaved.getId());
        verify(userRepository, times(1)).save(any());

        String email = user.getEmail();
        String error = String.format("Пользователь с email %s уже существует", email);
        when(userRepository.save(any())).thenThrow(new ObjectAlreadyExistsException(error));
        ObjectAlreadyExistsException exception = assertThrows(
                ObjectAlreadyExistsException.class,
                () -> userService.createUser(userDto)
        );
        assertEquals(error, exception.getMessage());
    }

    @Test
    void patch() {
        long userIdNotFound = 0L;
        String error = String.format("Пользователь с айди %d не найден.", userIdNotFound);
        when(userRepository.findById(userIdNotFound)).thenReturn(Optional.empty());
        ObjectNotFoundException exception = assertThrows(
                ObjectNotFoundException.class,
                () -> userService.getUserById(userIdNotFound));
        assertEquals(error, exception.getMessage());

        long userId = user.getId();
        String nameUpdated = "nameUpdated";
        User userUpdated = new User();
        userUpdated.setId(userId);
        userUpdated.setName(nameUpdated);
        userUpdated.setEmail(user.getEmail());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(userUpdated);
        UserDto userDtoUpdated = userService.patchUser(userId, UserDto.builder().name(nameUpdated).build());

        assertNotNull(userDtoUpdated);
        assertEquals(userId, userDtoUpdated.getId());
        assertEquals(nameUpdated, userDtoUpdated.getName());

        String emailUpdated = "updated@mail.ru";
        userUpdated.setEmail(emailUpdated);
        when(userRepository.save(any())).thenReturn(userUpdated);
        userDtoUpdated = userService.patchUser(userId, UserDto.builder().email(emailUpdated).build());

        assertNotNull(userDtoUpdated);
        assertEquals(userId, userDtoUpdated.getId());
        assertEquals(emailUpdated, userDtoUpdated.getEmail());

        String parameterName = "Имя";
        error = String.format("%s не может быть пустым", parameterName);
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> userService.patchUser(userId, UserDto.builder().name("").build())
        );
        assertEquals(error, ex.getMessage());

        parameterName = "E-mail";
        error = String.format("%s не может быть пустым", parameterName);
        ex = assertThrows(
                ValidationException.class,
                () -> userService.patchUser(userId, UserDto.builder().email("").build())
        );
        assertEquals(error, ex.getMessage());
    }

    @Test
    void delete() {
        long userId = 1L;
        userService.deleteUser(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }
}

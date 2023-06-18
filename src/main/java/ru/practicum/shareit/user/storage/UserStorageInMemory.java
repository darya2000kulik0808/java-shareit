package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ObjectAlreadyExistsException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
public class UserStorageInMemory implements UserStorage {

    private final Map<Long, User> idUsers = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private final Map<Long, String> userEmails = new HashMap<>();
    private long id = 1;

    @Override
    public User createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        if (!idUsers.containsKey(user.getId())) {
            user.setId(id);
            idUsers.put(user.getId(), user);
            emails.add(user.getEmail());
            userEmails.put(user.getId(), user.getEmail());
            id++;
            return user;
        }
        throw new ObjectAlreadyExistsException(
                String.format("Пользователь с таким id (%d) уже существует.", user.getId()));
    }

    @Override
    public User updateUser(User user) {
        if (idUsers.containsKey(user.getId())) {
            long id = user.getId();
            String email = user.getEmail();
            String oldEmail = userEmails.get(id);
            if (!email.equals(oldEmail)) {
                emails.remove(oldEmail);
                emails.add(email);
                userEmails.put(id, email);
            }
            idUsers.put(user.getId(), user);
            return user;
        }
        throw new ObjectNotFoundException(String.format("Пользователь с айди %d не найден.", user.getId()));
    }

    @Override
    public boolean deleteUser(long id) {
        if (idUsers.containsKey(id)) {
            emails.remove(idUsers.get(id).getEmail());
            userEmails.remove(id, idUsers.get(id).getEmail());
            idUsers.remove(id);
            return true;
        }
        throw new ObjectNotFoundException(String.format("Пользователь с айди %d не найден.", id));
    }

    @Override
    public Collection<User> getAllUsers() {
        if (!idUsers.values().isEmpty()) {
            return idUsers.values();
        }
        throw new ObjectNotFoundException("Список пользователей пуст.");
    }

    @Override
    public User getUserById(long userId) {
        if (idUsers.containsKey(userId)) {
            return idUsers.get(userId);
        }
        throw new ObjectNotFoundException(String.format("Пользователь с айди %d не найден.", userId));
    }

    @Override
    public boolean checkEmail(String email) {
        return emails.contains(email);
    }
}

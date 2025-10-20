package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class UserServiceIml implements UserService {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public UserDto createUser(UserDto userDto) {
        checkEmailExists(userDto.getEmail(), null);

        User user = UserMapper.toUser(userDto);
        user.setId(idCounter.getAndIncrement());
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = users.get(userId);
        if (existingUser == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            checkEmailExists(userDto.getEmail(), userId);
            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        users.put(userId, existingUser);
        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public User getUserEntityById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return user;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
    }

    private void checkEmailExists(String email, Long userId) {
        boolean emailExists = users.values().stream()
                .filter(user -> userId == null || !user.getId().equals(userId))
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));

        if (emailExists) {
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }
    }
}


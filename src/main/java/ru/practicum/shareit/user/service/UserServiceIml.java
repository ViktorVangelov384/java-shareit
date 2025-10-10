package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email обязателен");
        }

        if (!isValidEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Неверный формат email");
        }

        boolean emailExists = users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(userDto.getEmail()));
        if (emailExists) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = UserMapper.toUser(userDto);
        user.setId(idCounter.getAndIncrement());
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = users.get(userId);
        if (existingUser == null) {
            throw new RuntimeException("User not found");
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            if (!isValidEmail(userDto.getEmail())) {
                throw new IllegalArgumentException("Неверный формат email");
            }

            boolean emailExists = users.values().stream()
                    .filter(user -> !user.getId().equals(userId))
                    .anyMatch(user -> user.getEmail().equalsIgnoreCase(userDto.getEmail()));
            if (emailExists) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }

        users.put(userId, existingUser);
        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public User getUserEntityById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
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

    private boolean isValidEmail(String email) {
        return email != null &&
                email.contains("@") &&
                email.indexOf("@") < email.lastIndexOf(".") &&
                email.length() > 5;
    }
}


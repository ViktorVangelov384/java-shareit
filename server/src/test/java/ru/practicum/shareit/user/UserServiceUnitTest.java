package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceIml;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceIml userService;


    @Test
    void createUser_shouldCreateUser() {
        UserDto userDto = new UserDto(null, "John Doe", "john@email.com");
        User savedUser = new User(1L, "John Doe", "john@email.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.createUser(userDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_withValidData_shouldCreateUser() {
        UserDto userDto = new UserDto(null, "John Doe", "john@email.com");
        User savedUser = new User(1L, "John Doe", "john@email.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.createUser(userDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@email.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getUserById_withNonExistentUser_shouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    void updateUser_withNonExistentUser_shouldThrowNotFoundException() {
        UserDto updateDto = new UserDto(999L, "Updated Name", "updated@email.com");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    void updateUser_withPartialUpdate_shouldUpdateOnlyProvidedFields() {
        User existingUser = new User(1L, "John Doe", "john@email.com");
        UserDto updateDto = new UserDto(1L, "John Updated", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, updateDto);

        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("john@email.com");
    }

    @Test
    void updateUser_withAllFields_shouldUpdateAllFields() {
        User existingUser = new User(1L, "John Doe", "john@email.com");
        UserDto updateDto = new UserDto(1L, "John Updated", "john.updated@email.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, updateDto);

        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("john.updated@email.com");
    }

    @Test
    void updateUser_withOnlyEmail_shouldUpdateOnlyEmail() {
        User existingUser = new User(1L, "John Doe", "john@email.com");
        UserDto updateDto = new UserDto(1L, null, "john.updated@email.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, updateDto);

        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john.updated@email.com");
    }

    @Test
    void updateUser_withValidData_shouldUpdateUser() {
        User existingUser = new User(1L, "John Doe", "john@email.com");
        UserDto updateDto = new UserDto(1L, "John Updated", "john.updated@email.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, updateDto);

        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("john.updated@email.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        User user1 = new User(1L, "John Doe", "john@email.com");
        User user2 = new User(2L, "Jane Doe", "jane@email.com");
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        assertThat(result.get(1).getName()).isEqualTo("Jane Doe");
    }

    @Test
    void deleteUser_shouldCallRepository() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_withNonExistentUser_shouldThrowNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }
}


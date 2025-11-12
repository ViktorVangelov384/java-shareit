package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceIml;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserServiceIml.class)
class UserServiceIntegrationTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private UserServiceIml userService;
    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(null, "John Doe", "john@email.com");
        userRepository.save(user);
    }

    @Test
    void createUser_shouldSaveUserToDatabase() {
        UserDto userDto = new UserDto(null, "Jane Doe", "jane@email.com");

        UserDto result = userService.createUser(userDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("jane@email.com");

        User savedUser = em.find(User.class, result.getId());
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("Jane Doe");
        assertThat(savedUser.getEmail()).isEqualTo("jane@email.com");
    }

    @Test
    void getUserById_shouldReturnUser() {
        UserDto result = userService.getUserById(user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@email.com");
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        User user2 = new User(null, "Jane Doe", "jane@email.com");
        userRepository.save(user2);

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDto::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Doe");
    }

    @Test
    void updateUser_shouldUpdateUserInDatabase() {
        UserDto userDto = new UserDto(user.getId(), "John Updated", "john.updated@email.com");

        UserDto result = userService.updateUser(user.getId(), userDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("john.updated@email.com");

        User updatedUser = em.find(User.class, user.getId());
        assertThat(updatedUser.getName()).isEqualTo("John Updated");
        assertThat(updatedUser.getEmail()).isEqualTo("john.updated@email.com");
    }

    @Test
    void updateUser_withPartialData_shouldUpdateOnlyProvidedFields() {
        UserDto userDto = new UserDto(user.getId(), "John Updated", null);

        UserDto result = userService.updateUser(user.getId(), userDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("john@email.com");

        User updatedUser = em.find(User.class, user.getId());
        assertThat(updatedUser.getName()).isEqualTo("John Updated");
        assertThat(updatedUser.getEmail()).isEqualTo("john@email.com");
    }

    @Test
    void deleteUser_shouldRemoveUserFromDatabase() {
        userService.deleteUser(user.getId());

        User deletedUser = em.find(User.class, user.getId());
        assertThat(deletedUser).isNull();
    }
}

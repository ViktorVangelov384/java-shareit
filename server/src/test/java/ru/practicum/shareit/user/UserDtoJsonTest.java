package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testUserDtoSerialize() throws Exception {
        UserDto userDto = new UserDto(1L, "John Doe", "john@email.com");

        var result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("john@email.com");
    }

    @Test
    void testUserDtoDeserialize() throws Exception {
        String content = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john@email.com\"}";

        UserDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@email.com");
    }

    @Test
    void testUserDtoDeserializeWithPartialData() throws Exception {
        String content = "{\"name\":\"John Doe\",\"email\":\"john@email.com\"}";

        UserDto result = json.parseObject(content);

        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@email.com");
    }
}

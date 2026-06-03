package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserMapperTest {

    @Test
    void toUserDto_ShouldMapAllFields() {
        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@example.com");

        UserDto dto = UserMapper.toUserDto(user);

        assertEquals(1L, dto.getId());
        assertEquals("John", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
    }

    @Test
    void toUserDto_WithNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> UserMapper.toUserDto(null));
    }

    @Test
    void toUser_ShouldMapAllFields() {
        UserDto dto = new UserDto(1L, "John", "john@example.com");

        User user = UserMapper.toUser(dto);

        assertEquals(1L, user.getId());
        assertEquals("John", user.getName());
        assertEquals("john@example.com", user.getEmail());
    }
}
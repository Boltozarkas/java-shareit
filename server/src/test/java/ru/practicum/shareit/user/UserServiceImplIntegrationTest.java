package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Test
    void createUser_ShouldReturnUserDto() {
        UserDto userDto = new UserDto(null, "John Doe", "john@example.com");
        UserDto created = userService.create(userDto);

        assertNotNull(created.getId());
        assertEquals("John Doe", created.getName());
        assertEquals("john@example.com", created.getEmail());
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        UserDto userDto1 = new UserDto(null, "John Doe", "john@example.com");
        userService.create(userDto1);

        UserDto userDto2 = new UserDto(null, "Jane Doe", "john@example.com");
        assertThrows(DuplicateEmailException.class, () -> userService.create(userDto2));
    }

    @Test
    void updateUser_ShouldUpdateFields() {
        UserDto created = userService.create(new UserDto(null, "John Doe", "john@example.com"));

        UserDto updateDto = new UserDto(null, "John Updated", "john_updated@example.com");
        UserDto updated = userService.update(created.getId(), updateDto);

        assertEquals("John Updated", updated.getName());
        assertEquals("john_updated@example.com", updated.getEmail());
    }

    @Test
    void updateUser_WithNonExistentId_ShouldThrowException() {
        UserDto updateDto = new UserDto(null, "John Updated", "john_updated@example.com");
        assertThrows(NotFoundException.class, () -> userService.update(999L, updateDto));
    }

    @Test
    void getById_ShouldReturnUser() {
        UserDto created = userService.create(new UserDto(null, "John Doe", "john@example.com"));
        UserDto found = userService.getById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("John Doe", found.getName());
    }

    @Test
    void getAll_ShouldReturnAllUsers() {
        userService.create(new UserDto(null, "John Doe", "john@example.com"));
        userService.create(new UserDto(null, "Jane Doe", "jane@example.com"));

        List<UserDto> users = userService.getAll();
        assertEquals(2, users.size());
    }

    @Test
    void delete_ShouldRemoveUser() {
        UserDto created = userService.create(new UserDto(null, "John Doe", "john@example.com"));
        userService.delete(created.getId());

        assertThrows(NotFoundException.class, () -> userService.getById(created.getId()));
    }
}
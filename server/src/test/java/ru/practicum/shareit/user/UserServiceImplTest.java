package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");

        userDto = new UserDto(1L, "John Doe", "john@example.com");
    }

    @Test
    void create_ShouldReturnUserDto() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.create(new UserDto(null, "John Doe", "john@example.com"));

        assertNotNull(result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void create_WithDuplicateEmail_ShouldThrowException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateEmailException.class,
                () -> userService.create(new UserDto(null, "John Doe", "john@example.com")));
    }

    @Test
    void update_ShouldReturnUpdatedUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto updateDto = new UserDto(null, "John Updated", "john_updated@example.com");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        UserDto result = userService.update(1L, updateDto);

        assertEquals("John Updated", result.getName());
        assertEquals("john_updated@example.com", result.getEmail());
    }

    @Test
    void update_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.update(999L, new UserDto(null, "John", "john@example.com")));
    }

    @Test
    void getById_ShouldReturnUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserDto result = userService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void getById_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    void getAll_ShouldReturnAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Doe");
        user2.setEmail("jane@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        List<UserDto> result = userService.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void delete_ShouldDeleteUser() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyLong());

        assertDoesNotThrow(() -> userService.delete(1L));
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.delete(999L));
    }
}
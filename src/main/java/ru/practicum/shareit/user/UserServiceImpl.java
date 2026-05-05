package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @Override
    public UserDto create(UserDto userDto) {
        // Проверка на наличие email
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        // Проверка валидности email
        if (!userDto.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (existsByEmail(userDto.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + userDto.getEmail());
        }
        User user = UserMapper.toUser(userDto);
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User existingUser = users.get(userId);
        if (existingUser == null) {
            throw new NotFoundException("User not found: " + userId);
        }

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            if (existsByEmail(userDto.getEmail())) {
                throw new DuplicateEmailException("Email already exists: " + userDto.getEmail());
            }
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("User not found: " + userId);
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return new ArrayList<>(users.values()).stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
        users.remove(userId);
    }

    private boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(u -> u.getEmail().equals(email));
    }
}
package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        validateEmailUnique(userDto.getEmail());
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto update(Long userId, UserDto userDto) {
        User existingUser = getUserById(userId);

        Optional.ofNullable(userDto.getEmail())
                .filter(email -> !email.equals(existingUser.getEmail()))
                .ifPresent(email -> {
                    validateEmailUnique(email);
                    existingUser.setEmail(email);
                });

        Optional.ofNullable(userDto.getName())
                .ifPresent(name -> {
                    validateNameNotBlank(name);
                    existingUser.setName(name);
                });

        return UserMapper.toUserDto(userRepository.save(existingUser));
    }

    @Override
    public UserDto getById(Long userId) {
        User user = getUserById(userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        validateUserExists(userId);
        userRepository.deleteById(userId);
    }

    // ===== Методы получения сущностей =====

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    // ===== Методы валидации =====

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
    }

    private void validateEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already exists: " + email);
        }
    }

    private void validateNameNotBlank(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
    }
}
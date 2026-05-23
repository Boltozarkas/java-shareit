package ru.practicum.shareit.user.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Optional;

@UtilityClass
public class UserMapper {

    public UserDto toUserDto(User user) {
        return Optional.ofNullable(user)
                .map(u -> new UserDto(u.getId(), u.getName(), u.getEmail()))
                .orElseThrow(() -> new IllegalArgumentException("User cannot be null"));
    }

    public User toUser(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        return user;
    }
}
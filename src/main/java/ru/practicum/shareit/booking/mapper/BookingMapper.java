package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Optional;

@UtilityClass
public class BookingMapper {

    public BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());
        dto.setBooker(toUserDto(booking.getBooker()));
        dto.setItem(toItemDto(booking.getItem()));
        return dto;
    }

    private UserDto toUserDto(User user) {
        return Optional.ofNullable(user)
                .map(u -> {
                    UserDto dto = new UserDto();
                    dto.setId(u.getId());
                    dto.setName(u.getName());
                    dto.setEmail(u.getEmail());
                    return dto;
                })
                .orElseThrow(() -> new IllegalArgumentException("User cannot be null"));
    }

    private ItemDto toItemDto(Item item) {
        return Optional.ofNullable(item)
                .map(i -> {
                    ItemDto dto = new ItemDto();
                    dto.setId(i.getId());
                    dto.setName(i.getName());
                    return dto;
                })
                .orElseThrow(() -> new IllegalArgumentException("Item cannot be null"));
    }
}
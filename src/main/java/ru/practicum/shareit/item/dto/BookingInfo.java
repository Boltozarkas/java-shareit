package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingInfo {
    private Long id;
    private Long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
}
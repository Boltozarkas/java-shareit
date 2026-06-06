package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingCreateDto dto = new BookingCreateDto(1L, start, end);

        JsonContent<BookingCreateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isNotNull();
        assertThat(result).extractingJsonPathStringValue("$.end").isNotNull();
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"itemId\":1,\"start\":\"2024-01-01T10:00:00\",\"end\":\"2024-01-02T10:00:00\"}";

        BookingCreateDto dto = json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isNotNull();
        assertThat(dto.getEnd()).isNotNull();
    }
}
package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime created = LocalDateTime.now();
        CommentDto dto = new CommentDto(1L, "Great item!", "John Doe", created);

        JsonContent<CommentDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Great item!");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.created").isNotNull();
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"text\":\"Great item!\",\"authorName\":\"John Doe\",\"created\":\"2024-01-01T10:00:00\"}";

        CommentDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Great item!");
        assertThat(dto.getAuthorName()).isEqualTo("John Doe");
        assertThat(dto.getCreated()).isNotNull();
    }
}
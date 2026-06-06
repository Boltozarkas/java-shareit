package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem_ShouldReturn201() throws Exception {
        ItemDto inputDto = new ItemDto(null, "Item", "Description", true, null);
        ItemDto outputDto = new ItemDto(1L, "Item", "Description", true, null);

        when(itemService.create(anyLong(), any())).thenReturn(outputDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Item"));
    }

    @Test
    void updateItem_ShouldReturn200() throws Exception {
        ItemDto inputDto = new ItemDto(null, "Updated Item", "Updated Description", false, null);
        when(itemService.update(anyLong(), anyLong(), any())).thenReturn(inputDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Item"));
    }

    @Test
    void getById_ShouldReturn200() throws Exception {
        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Item");

        when(itemService.getById(anyLong(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void search_ShouldReturn200() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of(new ItemDto(1L, "Item", "Description", true, null)));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "Item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void addComment_ShouldReturn200() throws Exception {
        CommentCreateDto commentDto = new CommentCreateDto("Great item!");
        CommentDto resultDto = new CommentDto(1L, "Great item!", "User", null);

        when(itemService.addComment(anyLong(), anyLong(), anyString())).thenReturn(resultDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great item!"));
    }
}
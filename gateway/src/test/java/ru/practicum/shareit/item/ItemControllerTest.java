package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    // Успешные запросы
    @Test
    void createItem_ShouldReturn200() throws Exception {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, null);
        when(itemClient.createItem(anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void createItem_WithRequestId_ShouldReturn200() throws Exception {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, 1L);
        when(itemClient.createItem(anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_ShouldReturn200() throws Exception {
        ItemDto itemDto = new ItemDto(null, "Updated", "Updated Description", false, null);
        when(itemClient.updateItem(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getItem_ShouldReturn200() throws Exception {
        when(itemClient.getItem(anyLong(), anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getItemsByOwner_ShouldReturn200() throws Exception {
        when(itemClient.getItemsByOwner(anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_ShouldReturn200() throws Exception {
        when(itemClient.searchItems(anyLong(), anyString())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_ShouldReturn200() throws Exception {
        CommentCreateDto commentDto = new CommentCreateDto("Great item!");
        when(itemClient.addComment(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }

    // Ошибки валидации
    @Test
    void createItem_WithEmptyName_ShouldReturn400() throws Exception {
        ItemDto itemDto = new ItemDto(null, "", "Description", true, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_WithEmptyDescription_ShouldReturn400() throws Exception {
        ItemDto itemDto = new ItemDto(null, "Item", "", true, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_WithNullAvailable_ShouldReturn400() throws Exception {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", null, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_WithoutHeader_ShouldReturn400() throws Exception {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, null);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_WithEmptyText_ShouldReturn400() throws Exception {
        CommentCreateDto commentDto = new CommentCreateDto("");

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }
}
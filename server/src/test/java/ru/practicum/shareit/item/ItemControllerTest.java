package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemInDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    private static final String URL = "/items";
    private static final int SIZE_DEFAULT = 10;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemService;

    @Autowired
    private MockMvc mvc;

    private ItemInDto itemInDto;
    private ItemInDto.ItemInDtoBuilder itemInDtoBuilder;
    private ItemOutDto.ItemOutDtoBuilder itemOutDtoBuilder;
    private CommentDto.CommentDtoBuilder commentDtoBuilder;

    @BeforeEach
    void setupBuilder() {
        itemInDtoBuilder = ItemInDto.builder()
                .name("name")
                .description("description")
                .available(true);
        itemOutDtoBuilder = ItemOutDto.builder()
                .name("name")
                .description("description")
                .available(true);
        commentDtoBuilder = CommentDto.builder()
                .text("comment");
    }

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mvc);
    }

    @Test
    void shouldFindAllByUserId() throws Exception {
        when(itemService.getAllByUserId(1L, 0, SIZE_DEFAULT)).thenReturn(new ArrayList<>());
        mvc
                .perform(get(URL)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        ItemOutDto itemDto = itemOutDtoBuilder.id(1L).build();
        when(itemService.getAllByUserId(1L, 0, 1)).thenReturn(List.of(itemDto));
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName()), String.class));

        String error = "Отсутствует заголовок c идентификатором пользователя.";
        mvc.perform(get(URL))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));
    }

    @Test
    void shouldFindById() throws Exception {
        ItemOutDto itemBookingCommentsDto = itemOutDtoBuilder.id(1L).build();
        String json = mapper.writeValueAsString(itemBookingCommentsDto);

        when(itemService.getItemById(1L, 1L)).thenReturn(itemBookingCommentsDto);
        mvc.perform(get(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        String error = "Объект не найден";
        String description = "Вещь с id 1 не найдена";
        when(itemService.getItemById(1L, 1L)).thenThrow(new ObjectNotFoundException(description));
        mvc.perform(get(URL + "/1")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(error)))
                .andExpect(jsonPath("$.description", containsString(description)));
    }

    @Test
    void shouldFindByText() throws Exception {
        when(itemService.getByText("", 0, SIZE_DEFAULT)).thenReturn(new ArrayList<>());
        mvc.perform(get(URL + "/search")
                        .param("text", ""))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        ItemOutDto itemDto = itemOutDtoBuilder.id(1L).name("Отвертка").build();
        when(itemService.getByText("ОтВ", 0, 1)).thenReturn(List.of(itemDto));
        mvc.perform(get(URL + "/search")
                        .param("text", "ОтВ")
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName()), String.class));
    }

    @Test
    void shouldAdd() throws Exception {
        long userId = 1L;
        itemInDto = itemInDtoBuilder.build();
        ItemOutDto itemDtoAdded = itemOutDtoBuilder.id(1L).build();

        String json = mapper.writeValueAsString(itemInDto);
        String jsonAdded = mapper.writeValueAsString(itemDtoAdded);

        when(itemService.createItem(itemInDto, userId)).thenReturn(itemDtoAdded);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void shouldPatch() throws Exception {
        String json = "{\"name\": \"namePatched\"}";
        itemInDto = ItemInDto.builder().name("namePatched").build();
        ItemOutDto itemDtoPatched = itemOutDtoBuilder.id(1L).name("namePatched").build();
        String jsonPatched = mapper.writeValueAsString(itemDtoPatched);
        System.out.println(jsonPatched);
        when(itemService.updateItem(itemInDto, 1L, 1L)).thenReturn(itemDtoPatched);
        mvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonPatched));

        json = "{\"description\": \"descriptionPatched\"}";
        itemInDto = ItemInDto.builder().description("descriptionPatched").build();
        itemDtoPatched = itemOutDtoBuilder.description("descriptionPatched").build();
        jsonPatched = mapper.writeValueAsString(itemDtoPatched);
        when(itemService.updateItem(itemInDto, 1L, 1L)).thenReturn(itemDtoPatched);
        mvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonPatched));

        json = "{\"available\": \"false\"}";
        itemInDto = ItemInDto.builder().available(false).build();
        itemDtoPatched = ItemOutDto.builder().available(false).build();
        jsonPatched = mapper.writeValueAsString(itemInDto);
        when(itemService.updateItem(itemInDto, 1L, 1L)).thenReturn(itemDtoPatched);
        mvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonPatched));
    }

    @Test
    void shouldDeleteItem() throws Exception {
        mvc.perform(delete(URL + "/1")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldAddComment() throws Exception {
        CommentDto commentDto = commentDtoBuilder.build();
        String jsonIn = mapper.writeValueAsString(commentDto);
        CommentDto commentDtoOut = commentDtoBuilder
                .id(1L)
                .authorName("name")
                .created(LocalDateTime.now())
                .build();
        String json = mapper.writeValueAsString(commentDtoOut);
        when(itemService.createComment(commentDto, 1L, 1L)).thenReturn(commentDtoOut);
        mvc.perform(post(URL + "/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonIn))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }
}

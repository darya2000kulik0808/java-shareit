package ru.practicum.shareit.itemRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {
    private static final String URL = "/requests";
    private static final int SIZE_DEFAULT = 10;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    private ItemRequestOutDto itemRequestOutDto;
    private ItemRequestOutDto.ItemRequestOutDtoBuilder itemRequestOutDtoBuilder;

    @BeforeEach
    void setupBuilder() {
        itemRequestOutDtoBuilder = ItemRequestOutDto.builder()
                .id(1L)
                .description("Нужен перфоратор для ремонта и шумоподавления соседей с дрелью.")
                .created(LocalDateTime.now());
    }

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mvc);
    }

    @Test
    void shouldAddRequest() throws Exception {
        ItemRequestInDto requestIn = ItemRequestInDto.builder()
                .build();
        String json = mapper.writeValueAsString(requestIn);
        String description = "Описание не может быть пустым";
        String error = "Произошла ошибка!";
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)))
                .andExpect(jsonPath("$.description", containsString(description)));

        requestIn.setDescription("Нужен перфоратор для ремонта и шумоподавления соседей с дрелью.");
        ItemRequestOutCreatedDto requestOutCreated = ItemRequestOutCreatedDto.builder()
                .id(1L)
                .description("Нужен перфоратор для ремонта и шумоподавления соседей с дрелью.")
                .created(LocalDateTime.now())
                .build();
        json = mapper.writeValueAsString(requestIn);
        when(itemRequestService.createRequest(1L, requestIn)).thenReturn(requestOutCreated);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestOutCreated.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestOutCreated.getDescription()), String.class));

        error = "Объект не найден";
        description = String.format("Пользователь с id %d не найден", -1);
        when(itemRequestService.createRequest(-1L, requestIn)).thenThrow(new ObjectNotFoundException(description));
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", -1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(error)))
                .andExpect(jsonPath("$.description", containsString(description)));
    }

    @Test
    void shouldFindAllByUserId() throws Exception {
        when(itemRequestService.getAllUsersRequests(2L)).thenReturn(new ArrayList<>());
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        itemRequestOutDto = itemRequestOutDtoBuilder.build();
        when(itemRequestService.getAllUsersRequests(1L)).thenReturn(List.of(itemRequestOutDto));
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestOutDto.getDescription())));

        String error = "Объект не найден";
        String description = String.format("Пользователь с id %d не найден", -1);
        when(itemRequestService.getAllUsersRequests(-1L)).thenThrow(new ObjectNotFoundException(description));
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", -1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(error)))
                .andExpect(jsonPath("$.description", containsString(description)));
    }

    @Test
    void shouldFindAll() throws Exception {
        when(itemRequestService.getAllRequests(1L, 0, SIZE_DEFAULT)).thenReturn(new ArrayList<>());
        mvc.perform(get(URL + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        itemRequestOutDto = itemRequestOutDtoBuilder.build();
        when(itemRequestService.getAllRequests(1L, 0, 1)).thenReturn(List.of(itemRequestOutDto));
        mvc.perform(get(URL + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestOutDto.getDescription())));

        String error = "Объект не найден";
        String description = String.format("Пользователь с id %d не найден", -1);
        when(itemRequestService.getAllRequests(-1L, 0, 1)).thenThrow(new ObjectNotFoundException(description));
        mvc.perform(get(URL + "/all")
                        .header("X-Sharer-User-Id", -1)
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(error)))
                .andExpect(jsonPath("$.description", containsString(description)));

        error = "Произошла ошибка!";
        description = "Индекс первого элемента не может быть отрицательным";
        mvc.perform(get(URL + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)))
                .andExpect(jsonPath("$.description", containsString(description)));
    }

    @Test
    void shouldFindById() throws Exception {
        itemRequestOutDto = itemRequestOutDtoBuilder.build();
        when(itemRequestService.getOneRequest(1L, 1L)).thenReturn(itemRequestOutDto);
        mvc.perform(get(URL + "/1")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestOutDto.getDescription()), String.class));

        String error = "Объект не найден";
        String description = String.format("Пользователь с id %d не найден", -1);
        when(itemRequestService.getOneRequest(1L, -1L)).thenThrow(new ObjectNotFoundException(description));
        mvc.perform(get(URL + "/1")
                        .header("X-Sharer-User-Id", -1))
                .andDo(print())
                .andExpect(jsonPath("$.error", containsString(error)))
                .andExpect(jsonPath("$.description", containsString(description)));

        error = "Объект не найден";
        description = String.format("Запрос с id %d не найден", 99);
        when(itemRequestService.getOneRequest(99L, 1L)).thenThrow(new ObjectNotFoundException(description));
        mvc.perform(get(URL + "/99")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(error)))
                .andExpect(jsonPath("$.description", containsString(description)));
    }
}

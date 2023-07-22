package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    private static final String URL = "/users";

    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserService userService;

    @Autowired
    private MockMvc mvc;

    private UserDto userDto;
    private UserDto.UserDtoBuilder userDtoBuilder;

    @BeforeEach
    void setupBuilder() {
        userDtoBuilder = UserDto.builder()
                .name("name")
                .email("e@mail.ru");
    }

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mvc);
    }

    @Test
    void shouldFindAll() throws Exception {
        when(userService.getAllUsers()).thenReturn(new ArrayList<>());
        this.mvc
                .perform(get(URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        userDto = userDtoBuilder.id(1L).build();
        when(userService.getAllUsers()).thenReturn(List.of(userDto));
        mvc.perform(get(URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto.getName()), String.class))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail()), String.class));
    }

    @Test
    void shouldFindById() throws Exception {
        userDto = userDtoBuilder.id(1L).build();
        String json = mapper.writeValueAsString(userDto);

        when(userService.getUserById(1L)).thenReturn(userDto);
        mvc.perform(get(URL + "/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        String error = "Объект не найден";
        String description = String.format("Пользователь с id %d не найден", 1);
        when(userService.getUserById(1L)).thenThrow(new ObjectNotFoundException(description));
        mvc.perform(get(URL + "/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(error)))
                .andExpect(jsonPath("$.description", containsString(description)));
    }

    @Test
    void shouldAdd() throws Exception {
        userDto = userDtoBuilder.build();
        UserDto userDtoAdded = userDtoBuilder.id(1L).build();

        String json = mapper.writeValueAsString(userDto);
        String jsonAdded = mapper.writeValueAsString(userDtoAdded);

        when(userService.createUser(userDto)).thenReturn(userDtoAdded);
        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));

        userDto = userDtoBuilder.name("").build();
        json = mapper.writeValueAsString(userDto);
        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description", containsString("Имя не может быть пустым")));

        userDto = userDtoBuilder.name("name").email("").build();
        json = mapper.writeValueAsString(userDto);
        this.mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description", containsString("Заполните поле электронной почты")));

        userDto = userDtoBuilder.email("email").build();
        json = mapper.writeValueAsString(userDto);
        this.mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description", containsString("Текст не является эл. почтой.")));
    }

    @Test
    void shouldPatch() throws Exception {
        String json = "{\"name\": \"namePatched\"}";
        userDto = UserDto.builder().name("namePatched").build();
        UserDto userDtoUpdated = userDtoBuilder.id(1L).name("namePatched").build();
        String jsonPatched = mapper.writeValueAsString(userDtoUpdated);
        when(userService.patchUser(1L, userDto)).thenReturn(userDtoUpdated);
        mvc.perform(patch(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(jsonPatched));

        json = "{\"email\": \"patched@mail.ru\"}";
        userDto = UserDto.builder().email("patched@mail.ru").build();
        userDtoUpdated = userDtoBuilder.email("patched@mail.ru").build();
        jsonPatched = mapper.writeValueAsString(userDtoUpdated);
        when(userService.patchUser(1L, userDto)).thenReturn(userDtoUpdated);
        mvc.perform(patch(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonPatched));

        json = "{\"email\": \"patched\"}";
        mvc.perform(patch(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description", containsString("Текст не является эл. почтой.")));

    }

    @Test
    void shouldDelete() throws Exception {
        this.mvc.perform(delete(URL + "/1"))
                .andDo(print())
                .andExpect(status().isOk());

    }
}

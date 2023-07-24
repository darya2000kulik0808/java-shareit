package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ValidationException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    private static final String URL = "/users";

    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserClient client;

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mvc);
    }

    @Test
    void shouldValidateAdd() throws Exception {
        //fail name
        UserDto userDto = UserDto.builder()
                .name("")
                .email("e@mail.ru")
                .build();
        String json = mapper.writeValueAsString(userDto);
        String error = "Имя не может быть пустым";
        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));

        //fail empty email
        userDto.setName("name");
        userDto.setEmail("");
        json = mapper.writeValueAsString(userDto);
        error = "E-mail не может быть пустым";
        this.mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));
    }

    @Test
    void shouldValidatePatch() throws Exception {
        //fail name
        UserDto userDto = UserDto.builder()
                .name("")
                .build();
        String json = mapper.writeValueAsString(userDto);
        String error = "Имя не может быть пустым";
        when(client.patchUser(1, userDto))
                .thenThrow(new ValidationException(error));
        mvc.perform(patch(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));

        //fail empty email
        UserDto userDto1 = UserDto.builder().build();
        userDto1.setName("name");
        userDto1.setEmail("");
        json = mapper.writeValueAsString(userDto1);
        error = "Email не может быть пустым";
        when(client.patchUser(1, userDto1))
                .thenThrow(new ValidationException(error));
        this.mvc.perform(patch(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));

        //fail некорректный email
        userDto.setEmail("patched");
        json = mapper.writeValueAsString(userDto);
        error = "Введен некорректный e-mail";
        mvc.perform(patch(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email", containsString(error)));
    }
}

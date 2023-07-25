package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.enums.StatusEnum;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.UnknownStateException;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingControllerTest {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private static final String URL = "/bookings";
    private static final int SIZE_DEFAULT = 10;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService service;

    @Autowired
    private MockMvc mvc;

    private BookingOutDto bookingOutDto;
    private BookingDto.BookingDtoBuilder builderIn;
    private BookingOutDto.BookingOutDtoBuilder builderOut;

    @BeforeEach
    void setupBuilder() {
        LocalDateTime now = LocalDateTime.now();
        UserDto.UserDtoBuilder userDtoBuilder = UserDto.builder()
                .id(1L)
                .name("name")
                .email("e@mail.ru");
        ItemOutDto.ItemOutDtoBuilder itemOutDtoBuilder = ItemOutDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true);
        builderIn = BookingDto.builder()
                .itemId(1L)
                .start(now.plusMinutes(1))
                .end(now.plusMinutes(2));
        builderOut = BookingOutDto.builder()
                .id(1L)
                .booker(userDtoBuilder.build())
                .item(itemOutDtoBuilder.build())
                .start(now.plusMinutes(1))
                .end(now.plusMinutes(2))
                .status(StatusEnum.WAITING);
    }

    @Test
    @Order(1)
    void shouldCreateMockMvc() {
        assertNotNull(mvc);
    }

    @Test
    @Order(3)
    void shouldFindBooking() throws Exception {
        bookingOutDto = builderOut.build();

        when(service.findBooking(1L, 1L)).thenReturn(bookingOutDto);
        mvc.perform(get(URL + "/1")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingOutDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingOutDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.start", containsString(bookingOutDto.getStart().format(formatter)), String.class))
                .andExpect(jsonPath("$.status", is(bookingOutDto.getStatus().toString()), String.class));

        String error = "Объект не найден";
        when(service.findBooking(1L, -1L)).thenThrow(new ObjectNotFoundException(error));
        mvc.perform(get(URL + "/1")
                        .header("X-Sharer-User-Id", -1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(error), String.class));

        error = "Объект не найден";
        when(service.findBooking(99L, 1L)).thenThrow(new ObjectNotFoundException(error));
        mvc.perform(get(URL + "/99")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(error), String.class));
    }

    @Test
    @Order(4)
    void shouldFindAllForUser() throws Exception {
        when(service.findAllForUser(1L, "REJECTED", 0, SIZE_DEFAULT))
                .thenReturn(Collections.emptyList());
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "rejected"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        bookingOutDto = builderOut.build();
        when(service.findAllForUser(1L, "WAITING", 0, 1))
                .thenReturn(List.of(bookingOutDto));
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingOutDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(bookingOutDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingOutDto.getStatus().toString()), String.class));

        String error = "Unknown state: UNSUPPORTED_STATUS";
        when(service.findAllForUser(1L, "UNKNOWN", 0, 1))
                .thenThrow(new UnknownStateException(error));
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "UNKNOWN")
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));
    }

    @Test
    @Order(5)
    void shouldFindAllForOwner() throws Exception {
        when(service.findAllForOwner(1L, "REJECTED", 0, SIZE_DEFAULT))
                .thenReturn(Collections.emptyList());
        mvc.perform(get(URL + "/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "rejected"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        bookingOutDto = builderOut.build();
        when(service.findAllForOwner(1L, "WAITING", 0, 1))
                .thenReturn(List.of(bookingOutDto));
        mvc.perform(get(URL + "/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingOutDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(bookingOutDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].start", containsString(
                        bookingOutDto.getStart().format(formatter))))
                .andExpect(jsonPath("$[0].status", is(bookingOutDto.getStatus().toString()), String.class));

        String error = "Unknown state: UNSUPPORTED_STATUS";
        when(service.findAllForOwner(1L, "UNKNOWN", 0, 1))
                .thenThrow(new UnknownStateException(error));
        mvc.perform(get(URL + "/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "UNKNOWN")
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));
    }

    @Test
    @Order(2)
    void shouldCreate() throws Exception {
        BookingDto bookingInDto = builderIn.build();
        bookingOutDto = builderOut.build();
        String json = mapper.writeValueAsString(bookingInDto);

        when(service.createBooking(bookingInDto, 1L)).thenReturn(bookingOutDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingOutDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingOutDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingOutDto.getStatus().toString()), String.class));

        String error = "Объект не найден";
        when(service.createBooking(bookingInDto, -1L)).thenThrow(new ObjectNotFoundException(error));
        this.mvc
                .perform(post(URL)
                        .header("X-Sharer-User-Id", -1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(error), String.class));

        error = "Объект не найден";
        bookingInDto.setItemId(99L);
        json = mapper.writeValueAsString(bookingInDto);
        when(service.createBooking(bookingInDto, 1L)).thenThrow(new ObjectNotFoundException(error));
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(error), String.class));
    }

    @Test
    @Order(6)
    void shouldApproveOrReject() throws Exception {
        bookingOutDto = builderOut.status(StatusEnum.APPROVED).build();
        when(service.approveOrRejectBooking(1L, true, 1L)).thenReturn(bookingOutDto);
        mvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingOutDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingOutDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.start", containsString(bookingOutDto.getStart().format(formatter))))
                .andExpect(jsonPath("$.status", is(bookingOutDto.getStatus().toString()), String.class));

        bookingOutDto = builderOut.status(StatusEnum.REJECTED).build();
        when(service.approveOrRejectBooking(1L, false, 1L)).thenReturn(bookingOutDto);
        mvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "false"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingOutDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingOutDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.start", containsString(
                        bookingOutDto.getStart().format(formatter))))
                .andExpect(jsonPath("$.status", is(bookingOutDto.getStatus().toString()), String.class));

        String error = String.format("Бронирование с id %d уже отклонено", 1);
        when(service.approveOrRejectBooking(1L, false, 1L)).thenThrow(new ValidationException(error));
        mvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "false"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description", containsString(error), String.class));
    }
}

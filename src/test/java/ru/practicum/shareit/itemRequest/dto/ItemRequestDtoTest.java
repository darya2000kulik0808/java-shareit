package ru.practicum.shareit.itemRequest.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoTest {

    @Autowired
    private JacksonTester<ItemRequestInDto> jsonIn;
    @Autowired
    private JacksonTester<ItemRequestOutDto> jsonOut;

    private long id;
    private String description;
    private LocalDateTime created;
    private List<ItemOutDto> items;

    @BeforeEach
    void setup() {
        id = 1L;
        description = "description";
        created = LocalDateTime.now();
        ItemOutDto itemDto = ItemOutDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .requestId(id)
                .build();
        items = List.of(itemDto);
    }

    @Test
    void testRequestSerialization() throws Exception {
        ItemRequestOutDto itemRequestDto = ItemRequestOutDto.builder()
                .id(id)
                .description(description)
                .created(created)
                .items(items)
                .build();
        JsonContent<ItemRequestOutDto> result = jsonOut.write(itemRequestDto);

        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(description);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("name");
    }

    @Test
    void testRequestDeserialization() throws Exception {
        String jsonContent = String.format("{\"description\": \"%s\"}", description);
        ItemRequestInDto result = this.jsonIn.parse(jsonContent).getObject();

        assertThat(result.getDescription()).isEqualTo(description);
    }
}

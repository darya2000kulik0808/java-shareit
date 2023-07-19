package ru.practicum.shareit.item.comment.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CommentDtoTest {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private JacksonTester<CommentDto> json;

    private String text;
    private long id;
    private String authorName;
    private LocalDateTime created;

    @BeforeEach
    void setup() {
        id = 1L;
        text = "text";
        authorName = "name";
        created = LocalDateTime.now();
    }

    @Test
    void testCommentSerialization() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .id(id)
                .text(text)
                .authorName(authorName)
                .created(created)
                .build();
        JsonContent<CommentDto> result = json.write(commentDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo((1));
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo(authorName);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo(text);
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(formatter.format(created));
    }

    @Test
    void testCommentDeserialization() throws Exception {
        String jsonContent = String.format("{\"text\":\"%s\"}", text);
        CommentDto result = this.json.parse(jsonContent).getObject();

        assertThat(result.getText()).isEqualTo(text);
    }
}

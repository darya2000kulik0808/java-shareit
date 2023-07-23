package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.StatusEnum;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemInDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceITTest {
    private static final int SIZE_DEFAULT = 10;

    private final EntityManager em;
    private final ItemService service;

    @Test
    void findAllByUserId() {
        User owner = makeUser("name", "e@mail.ru");
        em.persist(owner);

        User booker = makeUser("name1", "e1@mail.ru");
        em.persist(booker);

        List<ItemInDto> sourceItems = List.of(
                makeItemDto("name1", "description1"),
                makeItemDto("name2", "description2"),
                makeItemDto("name3", "description3")
        );
        List<Item> savedItems = new ArrayList<>();
        sourceItems.stream()
                .map(itemDto -> ItemMapper.toItem(itemDto, owner))
                .forEach(item -> {
                    em.persist(item);
                    savedItems.add(item);
                });

        Booking booking = makeNewBooking(LocalDateTime.now(), LocalDateTime.now().plusSeconds(60),
                savedItems.get(0), booker);
        em.persist(booking);
        Comment comment = makeComment("text", booker, savedItems.get(0));
        em.persist(comment);

        em.flush();

        List<ItemOutDto> targetItems =
                (List<ItemOutDto>) service.getAllByUserId(owner.getId(), 0, SIZE_DEFAULT);

        assertThat(targetItems, hasSize(sourceItems.size()));
        for (ItemInDto sourceRequest : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceRequest.getDescription()))
            )));
        }
        assertThat(targetItems, hasItem(
                hasProperty("comments", notNullValue())
        ));

        assertThat(targetItems, hasItem(
                hasProperty("lastBooking", notNullValue())
        ));
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private ItemInDto makeItemDto(String name, String description) {
        return ItemInDto.builder()
                .name(name)
                .description(description)
                .available(true)
                .build();
    }

    private Comment makeComment(String text, User author, Item item) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setUser(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    private Booking makeNewBooking(LocalDateTime start, LocalDateTime end, Item item, User booker) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(StatusEnum.WAITING);
        booking.setStart(start);
        booking.setEnd(end);
        return booking;
    }
}

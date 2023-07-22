package ru.practicum.shareit.itemRequest.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
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
public class ItemRequestServiceITTest {
    private static final int SIZE_DEFAULT = 10;

    private final EntityManager em;
    private final ItemRequestService service;

    @Test
    void findAll() {
        User requester = makeUser("name", "e@mail.ru");
        em.persist(requester);
        System.out.println(requester.getId());
        List<ItemRequestOutDto> sourceRequests = List.of(
                makeRequestDto("description1"),
                makeRequestDto("description2"),
                makeRequestDto("description1")
        );
        List<ItemRequest> savedRequests = new ArrayList<>();
        sourceRequests.stream()
                .map(ItemRequestMapper::toItemRequest)
                .forEach(request -> {
                    request.setRequester(requester);
                    request.setCreated(LocalDateTime.now().plusMinutes(10));
                    em.persist(request);
                    savedRequests.add(request);
                });
        User owner = makeUser("name1", "e1@mail.ru");
        em.persist(owner);
        ItemRequest request = savedRequests.get(0);
        Item item = makeAvailableItem("name", "description", owner, request);
        em.persist(item);

        em.flush();

        List<ItemRequestOutDto> targetRequests = service.getAllRequests(owner.getId(), 0, SIZE_DEFAULT);

        assertThat(targetRequests, hasSize(sourceRequests.size()));
        for (ItemRequestOutDto sourceRequest : sourceRequests) {
            assertThat(targetRequests, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceRequest.getDescription()))
            )));
        }
        assertThat(targetRequests, hasItem(
                hasProperty("items", notNullValue())
        ));
    }

    private ItemRequestOutDto makeRequestDto(String description) {
        return ItemRequestOutDto.builder()
                .description(description)
                .build();
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Item makeAvailableItem(String name, String description, User owner, ItemRequest request) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }
}

package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.StatusEnum;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceITTest {
    private final EntityManager em;
    private final BookingService service;

    @Test
    void findByState() {
        User owner = makeUser("name1", "e1@mail.ru");
        User booker = makeUser("name2", "e2@mail.ru");
        em.persist(owner);
        em.persist(booker);

        Item item = makeAvailableItem("name", "description", owner);
        em.persist(item);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> sourceBookings = List.of(
                makeBooking(now, now.plusMinutes(5), item, owner),
                makeBooking(now.plusMinutes(10), now.plusMinutes(15), item, owner),
                makeBooking(now.plusMinutes(20), now.plusMinutes(25), item, owner)
        );
        sourceBookings.forEach(booking -> {
            booking.setBooker(booker);
            booking.setStatus(StatusEnum.WAITING);
            em.persist(booking);
            booking.setItem(item);
        });

        em.flush();

        List<BookingOutDto> targetBookings = service.findAllForUser(booker.getId(),
                "ALL", 0, 10);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    private Booking makeBooking(LocalDateTime start,
                                LocalDateTime end,
                                Item item,
                                User user) {
        return Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(user)
                .status(StatusEnum.WAITING)
                .build();
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Item makeAvailableItem(String name, String description, User owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(true);
        item.setOwner(owner);
        return item;
    }
}

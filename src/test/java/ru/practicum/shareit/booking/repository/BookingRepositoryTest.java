package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.StatusEnum;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Booking booking;
    private Item item;

    @BeforeEach
    void setup() {
        User owner = new User();
        owner.setName("name");
        owner.setEmail("e@mail.ru");
        owner = userRepository.save(owner);

        User booker = new User();
        booker.setName("name1");
        booker.setEmail("e1@mail.ru");
        booker = userRepository.save(booker);

        item = new Item();
        item.setName("Набор отверток");
        item.setDescription("Большой набор отверток");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        LocalDateTime now = LocalDateTime.now();
        booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(now.plusSeconds(5));
        booking.setEnd(now.plusSeconds(60));
        booking.setStatus(StatusEnum.APPROVED);
        booking = bookingRepository.save(booking);
    }

    @Test
    public void testContextLoads() {
        assertNotNull(em);
    }

    @Test
    void testFindBookingsAtSameTime() {
        //пустой список
        LocalDateTime start = booking.getEnd().plusSeconds(5);
        LocalDateTime end = booking.getEnd().plusSeconds(25);
        StatusEnum status = StatusEnum.APPROVED;
        TypedQuery<Booking> query = em.getEntityManager()
                .createQuery("select b from Booking b where (b.item.id = :itemId) and " +
                        "(b.status = :status) and " +
                        "(b.start between :start and :end " +
                        "OR b.end between :start and :end " +
                        "OR b.start <= :start AND b.end >= :end)", Booking.class);
        List<Booking> bookings = query
                .setParameter("itemId", item.getId())
                .setParameter("status", status)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        assertNotNull(bookings);
        assertEquals(0, bookings.size());
        List<Booking> bookingsFound = bookingRepository.findBookingsAtSameTime(item.getId(), status, start, end);
        assertNotNull(bookingsFound);
        assertEquals(0, bookingsFound.size());

        //список из одного элемента
        start = booking.getStart().plusSeconds(5);
        bookings = query
                .setParameter("itemId", item.getId())
                .setParameter("status", status)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        bookingsFound = bookingRepository.findBookingsAtSameTime(item.getId(), status, start, end);
        assertNotNull(bookingsFound);
        assertEquals(1, bookingsFound.size());
        assertEquals(bookings.get(0).getId(), bookingsFound.get(0).getId());
    }

    @Test
    void testFindAllByItemOwnerId() {
        int pageNum = 0;
        int size = 1;
        PageRequest page = PageRequest.of(pageNum, size);

        //пустой список
        TypedQuery<Booking> query = em.getEntityManager()
                .createQuery("select b from Booking b " +
                        "where b.item.owner.id = :id ", Booking.class);
        List<Booking> bookings = query
                .setParameter("id", 99L)
                .getResultList();
        assertNotNull(bookings);
        assertEquals(0, bookings.size());
        List<Booking> bookingsFound = bookingRepository.findAllByItemOwner(99L, page).getContent();
        assertNotNull(bookingsFound);
        assertEquals(0, bookingsFound.size());

        //список из одного элемента
        query = em.getEntityManager()
                .createQuery("select b from Booking b " +
                        "where b.item.owner.id = :id ", Booking.class);
        bookings = query
                .setParameter("id", item.getOwner().getId())
                .getResultList();
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        bookingsFound = bookingRepository.findAllByItemOwner(item.getOwner().getId(), page).getContent();
        assertNotNull(bookingsFound);
        assertEquals(1, bookingsFound.size());
        assertEquals(bookings.get(0).getId(), bookingsFound.get(0).getId());
    }
}

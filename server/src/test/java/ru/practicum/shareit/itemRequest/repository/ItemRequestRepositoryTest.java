package ru.practicum.shareit.itemRequest.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ItemRequestRepositoryTest {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private ItemRequest request;
    private User requester;

    @BeforeEach
    void setup() {
        requester = new User();
        requester.setName("name");
        requester.setEmail("e@mail.ru");

        request = new ItemRequest();
        request.setDescription("description");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());
    }

    @Test
    public void contextLoads() {
        assertNotNull(em);
    }

    @Test
    void verifyBootstrappingByPersistingRequest() {
        assertNull(request.getId());
        em.persist(requester);
        em.persist(request);
        assertNotNull(request.getId());
    }

    @Test
    void verifyRepositoryByPersistingRequest() {
        assertNull(request.getId());
        userRepository.save(requester);
        itemRequestRepository.save(request);
        assertNotNull(request.getId());
    }

    @Test
    void shouldFindByRequesterId() {
        //пустой спикок
        List<ItemRequest> requests = itemRequestRepository.findAllByRequester_Id(1L);
        assertNotNull(requests);
        assertEquals(0, requests.size());

        //список из одного элемента
        em.persist(requester);
        em.persist(request);
        requests = itemRequestRepository.findAllByRequester_Id(requester.getId());
        assertNotNull(requests);
        assertEquals(1, requests.size());
    }

    @Test
    void shouldFindByRequesterIdNot() {
        int pageNum = 0;
        int size = 1;
        PageRequest page = PageRequest.of(pageNum, size, SORT);

        //пустой спикок
        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdNot(1L, page).getContent();
        assertNotNull(requests);
        assertEquals(0, requests.size());

        //список из одного элемента
        em.persist(requester);
        em.persist(request);
        requests = itemRequestRepository.findByRequesterIdNot(2L, page).getContent();
        assertEquals(1, requests.size());

        //постранично, с сортировкой
        ItemRequest request2 = new ItemRequest();
        request2.setDescription("description2");
        request2.setRequester(requester);
        request2.setCreated(LocalDateTime.now().plusMinutes(20));
        em.persist(request2);
        requests = itemRequestRepository.findByRequesterIdNot(2L, page).getContent();
        assertEquals(1, requests.size());
        assertEquals("description2", requests.get(0).getDescription());
    }
}

package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private Item item;
    private ItemRequest request;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setName("name");
        owner.setEmail("e@mail.ru");
        owner = userRepository.save(owner);

        User requester = new User();
        requester.setName("name1");
        requester.setEmail("e1@mail.ru");
        requester = userRepository.save(requester);

        request = new ItemRequest();
        request.setDescription("description");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());
        request = itemRequestRepository.save(request);

        item = new Item();
        item.setName("Набор отверток");
        item.setDescription("Набор отверток, большой, 24 штуки");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        item = itemRepository.save(item);
    }

    @Test
    public void testContextLoads() {
        assertNotNull(em);
    }

    @Test
    void testFindByOwnerId() {
        int pageNum = 0;
        int size = 1;
        PageRequest page = PageRequest.of(pageNum, size);

        //пустой спикок
        List<Item> items = itemRepository.findAllByOwner_Id(0L, page).getContent();
        assertNotNull(items);
        assertEquals(0, items.size());

        //список из одного элемента
        items = itemRepository.findAllByOwner_Id(owner.getId(), page).getContent();
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());


        Item item1 = new Item();
        item1.setOwner(owner);
        item1.setAvailable(true);
        item1.setName("Дрель");
        item1.setDescription("Дрель обыкновенная, станьте шумным врагом подъезда.");
        itemRepository.save(item1);

        items = itemRepository.findAllByOwner_Id(owner.getId(), page).getContent();
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());

        pageNum = 1;
        page = PageRequest.of(pageNum, size);
        items = itemRepository.findAllByOwner_Id(owner.getId(), page).getContent();
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item1.getId(), items.get(0).getId());
    }

    @Test
    void testSearching() {
        int pageNum = 0;
        int size = 1;
        PageRequest page = PageRequest.of(pageNum, size);

        //пустой спикок
        String text = "тыква";
        TypedQuery<Item> query = em.getEntityManager()
                .createQuery(" select i from Item i " +
                        "where (lower(i.name) like concat('%', :text, '%') " +
                        " or lower(i.description) like concat('%', :text, '%')) " +
                        " and i.available = true", Item.class);
        List<Item> items = query.setParameter("text", text).getResultList();
        assertEquals(0, items.size());
        List<Item> itemsSearch = itemRepository.search(text, page).getContent();
        assertNotNull(itemsSearch);
        assertEquals(0, itemsSearch.size());

        //список из одного элемента
        text = "отв";
        items = query.setParameter("text", text).getResultList();
        assertEquals(1, items.size());
        itemsSearch = itemRepository.search(text, page).getContent();
        assertNotNull(itemsSearch);
        assertEquals(1, itemsSearch.size());
        assertEquals(items.get(0).getId(), itemsSearch.get(0).getId());

        Item item1 = new Item();
        item1.setOwner(owner);
        item1.setAvailable(true);
        item1.setName("Дрель");
        item1.setDescription("Дрель обыкновенная - станьте ответственным за шум в подъезде.");
        itemRepository.save(item1);

        itemsSearch = itemRepository.search(text, page).getContent();
        assertNotNull(itemsSearch);
        assertEquals(1, itemsSearch.size());

        size = 2;
        page = PageRequest.of(pageNum, size);
        itemsSearch = itemRepository.search(text, page).getContent();
        assertNotNull(itemsSearch);
        assertEquals(2, itemsSearch.size());
    }

    @Test
    void testFindByRequestId() {
        //пустой спикок
        List<Item> items = itemRepository.findAllByRequest_Id(0L);
        assertNotNull(items);
        assertEquals(0, items.size());

        //список из одного элемента
        items = itemRepository.findAllByRequest_Id(request.getId());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
    }

    @Test
    void testFindByRequestIdIn() {
        //пустой спикок
        List<Item> items = itemRepository.findByRequestIdIn(List.of(0L));
        assertNotNull(items);
        assertEquals(0, items.size());

        //список из одного элемента
        items = itemRepository.findByRequestIdIn(List.of(request.getId()));
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
    }
}

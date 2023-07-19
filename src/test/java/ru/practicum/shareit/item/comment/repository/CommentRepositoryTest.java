package ru.practicum.shareit.item.comment.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class CommentRepositoryTest {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");
    @Autowired
    private TestEntityManager em;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    private Comment comment;

    @BeforeEach
    void setup() {
        User author = new User();
        author.setName("name");
        author.setEmail("e@mail.ru");

        User owner = new User();
        owner.setName("name2");
        owner.setEmail("e2@mail.ru");

        Item item = new Item();
        item.setName("name");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(owner);

        comment = new Comment();
        comment.setText("comment");
        comment.setItem(item);
        comment.setUser(author);
        comment.setCreated(LocalDateTime.now());

        userRepository.save(author);
        userRepository.save(owner);
        itemRepository.save(item);
        commentRepository.save(comment);
    }

    @Test
    public void contextLoads() {
        assertNotNull(em);
    }

    @Test
    void findAllByItemId() {
        //пустой список
        List<Comment> comments = commentRepository.findAllByItem_Id(99L, SORT);
        comments = comments.stream().sorted(Comparator.comparing(Comment::getCreated)).collect(Collectors.toList());
        assertNotNull(comments);
        assertEquals(0, comments.size());

        //список из одного элемента
        comments = commentRepository.findAllByItem_Id(comment.getItem().getId(), SORT);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(comments.get(0).getId(), comment.getId());

        //проверка отката транзакций
        comments = commentRepository.findAll();
        assertEquals(1, comments.size());
    }
}

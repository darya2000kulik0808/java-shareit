package ru.practicum.shareit.item.comment.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.comment.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>,
        CrudRepository<Comment, Long> {

    List<Comment> findAllByItem_Id(Long itemId, Sort sort);
}

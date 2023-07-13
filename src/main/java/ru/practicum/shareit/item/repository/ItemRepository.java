package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwner_Id(Long id);

    @Query("select i from Item i " +
            "where i.available = true " +
            "and (lower(i.description) like lower(concat('%', ?1, '%')) " +
            "or lower(i.name) like lower(concat('%', ?1, '%'))) ")
    List<Item> search(String text);
}

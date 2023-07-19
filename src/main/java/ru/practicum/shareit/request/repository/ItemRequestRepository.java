package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long>,
        CrudRepository<ItemRequest, Long>,
        PagingAndSortingRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByRequester_Id(Long requesterId);

    Page<ItemRequest> findByRequesterIdNot(Long userId, Pageable page);
}

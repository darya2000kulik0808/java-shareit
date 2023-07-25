package ru.practicum.shareit.itemRequest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    ItemRequestServiceImpl service;

    private User requester;
    private User owner;
    private Item item;
    private ItemRequest itemRequest;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setName("name");
        owner.setEmail("e@mail.ru");
        owner.setId(1L);

        requester = new User();
        requester.setName("name2");
        requester.setEmail("e2@mail.ru");
        requester.setId(2L);

        itemRequest = new ItemRequest();
        itemRequest.setDescription("description");
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setId(1L);

        item = new Item();
        item.setId(1L);
        item.setName("name");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(itemRequest);
    }


    @Test
    void add() {
        when(itemRequestRepository.save(any())).thenReturn(itemRequest);
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        ItemRequestOutCreatedDto requestDto = service.createRequest(requester.getId(),
                ItemRequestInDto.builder().description("description").build());
        assertNotNull(requestDto);
        assertEquals(itemRequest.getId(), requestDto.getId());
        verify(itemRequestRepository, times(1)).save(any());
    }

    @Test
    void addFailByUserNotFound() {
        long userNotFoundId = 0L;
        String error = String.format("Пользователь с id %d не найден", userNotFoundId);
        when(userRepository.findById(userNotFoundId)).thenThrow(new ObjectNotFoundException(error));
        ObjectNotFoundException exception = assertThrows(
                ObjectNotFoundException.class,
                () -> service.createRequest(userNotFoundId, ItemRequestInDto.builder().description("description").build())
        );
        assertEquals(error, exception.getMessage());
        verify(itemRequestRepository, times(0)).save(any());
    }

    @Test
    void findAllByUserId() {
        long userId = requester.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findAllByRequester_Id(userId)).thenReturn(List.of(itemRequest));
        List<ItemRequestOutDto> requests = service.getAllUsersRequests(userId);
        assertNotNull(requests);
        assertEquals(1, requests.size());
        verify(itemRequestRepository, times(1)).findAllByRequester_Id(userId);
    }

    @Test
    void findAll() {
        long userId = requester.getId();
        int from = 0;
        int size = 1;
        PageRequest page = PageRequest.of(from / size, size, SORT);
        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(itemRepository.findByRequestIdIn(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(itemRequestRepository.findByRequesterIdNot(userId, page)).thenReturn(Page.empty());
        List<ItemRequestOutDto> requestDtos = service.getAllRequests(userId, from, size);
        assertNotNull(requestDtos);
        assertEquals(0, requestDtos.size());

        userId = owner.getId();
        long requestId = itemRequest.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.findByRequestIdIn(List.of(requestId))).thenReturn(List.of(item));
        when(itemRequestRepository.findByRequesterIdNot(userId, page)).thenReturn(
                new PageImpl<>(List.of(itemRequest)));
        requestDtos = service.getAllRequests(userId, from, size);
        assertNotNull(requestDtos);
        assertEquals(1, requestDtos.size());
    }

    @Test
    void findById() {
        long userId = requester.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        long requestId = itemRequest.getId();
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequest_Id(requestId)).thenReturn(List.of(item));
        ItemRequestOutDto requestDto = service.getOneRequest(requestId, userId);

        assertNotNull(requestDto);
        assertEquals(requestId, requestDto.getId());
        assertEquals(1, requestDto.getItems().size());
        assertEquals(item.getId(), requestDto.getItems().get(0).getId());

        InOrder inOrder = inOrder(itemRequestRepository, userRepository, itemRepository);
        inOrder.verify(itemRequestRepository).findById(requestId);
        inOrder.verify(userRepository).findById(userId);
        inOrder.verify(itemRepository).findAllByRequest_Id(requestId);
    }
}
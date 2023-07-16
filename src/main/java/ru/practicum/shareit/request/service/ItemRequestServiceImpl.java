package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private static final Sort SORTED = Sort.by(Sort.Direction.DESC, "created");
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    @Override
    public List<ItemRequestOutDto> getAllUsersRequests(Long userId) {
        checkUser(userId);
        List<ItemRequestOutDto> itemRequestDtos = itemRequestRepository.findAllByRequester_Id(userId).stream()
                .map(ItemRequestMapper::toItemRequestOutDto).collect(Collectors.toList());
        addItemsToRequests(itemRequestDtos);
        return itemRequestDtos;
    }

    @Override
    public List<ItemRequestOutDto> getAllRequests(Long userId, Integer from, Integer size) {
        checkUser(userId);
        PageRequest page = PageRequest.of(from / size, size, SORTED);
        List<ItemRequestOutDto> itemRequestDtos = itemRequestRepository.findByRequesterIdNot(userId, page)
                .map(ItemRequestMapper::toItemRequestOutDto).getContent();
        addItemsToRequests(itemRequestDtos);
        return itemRequestDtos;
    }

    @Override
    public ItemRequestOutDto getOneRequest(Long requestId, Long userId) {
        ItemRequest itemRequest = checkRequest(requestId);
        checkUser(userId);
        List<ItemOutDto> items = itemRepository.findAllByRequest_Id(requestId)
                .stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        ItemRequestOutDto itemRequestOutDto = ItemRequestMapper.toItemRequestOutDto(itemRequest);
        itemRequestOutDto.setItems(items);
        return itemRequestOutDto;
    }

    @Override
    public ItemRequestOutCreatedDto createRequest(Long userId, ItemRequestInDto itemRequestInDto) {
        User user = checkUser(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestInDto, user, LocalDateTime.now());
        return ItemRequestMapper.toItemRequestOutCreatedDto(itemRequestRepository.save(itemRequest));
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }

    private ItemRequest checkRequest(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Запрос с id %d не найден", requestId)));
    }

    private void addItemsToRequests(List<ItemRequestOutDto> itemRequestDtos) {
        List<Long> requestIds = itemRequestDtos.stream().map(ItemRequestOutDto::getId).collect(Collectors.toList());
        List<ItemOutDto> itemDtos = itemRepository.findByRequestIdIn(requestIds).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());

        if (itemDtos.isEmpty()) {
            itemRequestDtos.forEach(itemRequestOutDto -> itemRequestOutDto.setItems(Collections.emptyList()));
        }
        Map<Long, ItemRequestOutDto> requests = new HashMap<>();
        Map<Long, List<ItemOutDto>> items = new HashMap<>();

        itemDtos.forEach(itemDto -> items.computeIfAbsent(itemDto.getRequestId(), key -> new ArrayList<>()).add(itemDto));
        itemRequestDtos.forEach(request -> requests.put(request.getId(), request));
        items.forEach((key, value) -> requests.get(key).setItems(value));
    }
}

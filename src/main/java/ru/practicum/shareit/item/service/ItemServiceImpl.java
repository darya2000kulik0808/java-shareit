package ru.practicum.shareit.item.service;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.enums.StatusEnum;
import ru.practicum.shareit.exceptions.CommentAccessDeniedException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@AllArgsConstructor
@Service
@Qualifier("ItemServiceImpl")
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User user = checkUser(userId);
        Item item = ItemMapper.toItem(itemDto, user);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long id, Long userId) {
        Item item = checkOwner(userId, id);
        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                throw new ValidationException("Название не может быть пустым");
            }
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                throw new ValidationException("Описание не может быть пустым");
            }
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getItemById(Long id, Long userId) {
        checkUser(userId);
        Item item = checkItem(id);

        ItemDto itemDto = ItemMapper.toItemDto(item);

        return setCommentsAndBookings(userId, itemDto);
    }

    @Override
    public Collection<ItemDto> getAllByUserId(Long userId) {
        checkUser(userId);

        List<ItemDto> items = itemRepository.findAllByOwner_Id(userId).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());

        List<ItemDto> itemOut = new ArrayList<>();

        for (ItemDto itemDto : items) {
            itemOut.add(setCommentsAndBookings(userId, itemDto));
        }
        return itemOut;
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        checkOwner(userId, itemId);
        itemRepository.deleteById(itemId);
    }

    @Override
    public Collection<ItemDto> getByText(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository
                .search(text)
                .stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public CommentDto createComment(CommentDto commentDto, Long userId, Long itemId) {
        User user = checkUser(userId);
        Item item = checkItem(itemId);
        List<Booking> bookingList = bookingRepository
                .findByBookerIdAndItemIdAndStatusAndStartIsBefore(userId,
                        itemId, StatusEnum.APPROVED, LocalDateTime.now());
        if (!bookingList.isEmpty()) {
            Comment comment = CommentMapper.toComment(commentDto, user);
            comment.setItem(item);
            comment.setUser(user);
            comment.setCreated(LocalDateTime.now());
            return CommentMapper.toCommentDto(commentRepository.save(comment));
        } else {
            throw new CommentAccessDeniedException("Вы не бронировали вещь. Оставить комментарий невозможно.");
        }
    }

    private ItemDto setCommentsAndBookings(Long userId, ItemDto itemDto) {
        itemDto.setComments(getComments(itemDto));
        if (userId.equals(itemDto.getOwner().getId())) {
            List<BookingDto> lastAndNextBookings = getLastAndNextBooking(itemDto);
            if (!lastAndNextBookings.isEmpty()) {
                itemDto.setLastBooking(lastAndNextBookings.get(0));
                if (!(lastAndNextBookings.size() == 1)) {
                    itemDto.setNextBooking(lastAndNextBookings.get(1));
                }
            }
        }
        return itemDto;
    }

    public List<CommentDto> getComments(ItemDto item) {
        List<CommentDto> commentList = commentRepository.findAllByItem_Id(item.getId())
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        if (commentList.isEmpty()) {
            return Collections.emptyList();
        }

        return commentList;
    }

    private List<BookingDto> getLastAndNextBooking(ItemDto item) {
        List<Booking> lastAndNext = new ArrayList<>();
        Booking next = null;
        Booking last = null;
        List<Booking> bookings = bookingRepository
                .findAllByItem_IdAndStatusOrderByStartDesc(item.getId(), StatusEnum.APPROVED, StatusEnum.WAITING);

        if (bookings.isEmpty()) {
            return Collections.emptyList();
        }

        for (Booking booking : bookings) {
            if (booking.getStart().isAfter(LocalDateTime.now())) {
                next = booking;
            }
            if (booking.getStart().isBefore(LocalDateTime.now())) {
                last = booking;
                break;
            }
        }

        lastAndNext.add(last);
        if (next != null) {
            lastAndNext.add(next);
        }
        return lastAndNext.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private Item checkOwner(Long userId, Long itemId) {
        checkUser(userId);
        Item item = checkItem(itemId);
        long ownerId = item.getOwner().getId();
        if (ownerId != userId) {
            throw new ObjectNotFoundException(
                    String.format("У пользователя с id %d нет вещи с id %d", userId, itemId));
        }
        return item;
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }

    private Item checkItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Вещь с id %d не найдена", itemId)));
    }
}

package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.enums.StatusEnum;
import ru.practicum.shareit.exceptions.CommentAccessDeniedException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemInDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    @Mock
    ItemRepository itemRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private Comment comment;

    @BeforeEach
    void setup() {
        LocalDateTime start = LocalDateTime.now().minusSeconds(120);
        LocalDateTime end = LocalDateTime.now().minusSeconds(60);
        owner = new User();
        owner.setName("name");
        owner.setEmail("e@mail.ru");
        owner.setId(1L);

        booker = new User();
        booker.setName("name2");
        booker.setEmail("e2@mail.ru");
        booker.setId(2L);

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Хочешь, задрелю соседей, что мешают спать?");
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(StatusEnum.APPROVED);

        comment = new Comment();
        comment.setText("text");
        comment.setUser(booker);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        comment.setId(1L);
    }

    @Test
    void findAllByUserId() {
        //Empty List
        long userId = booker.getId();
        int from = 0;
        int size = 1;
        PageRequest page = PageRequest.of(from / size, size);
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findAllByOwner_Id(userId, page)).thenReturn(Page.empty());
        List<ItemOutDto> itemDtos = new ArrayList<>(itemService.getAllByUserId(userId, from, size));
        assertNotNull(itemDtos);
        assertEquals(0, itemDtos.size());

        //Single List
        userId = owner.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(commentRepository.findAllByItem_Id(item.getId(), SORT))
                .thenReturn(List.of(comment));
        when(bookingRepository.findAllByItem_IdAndStatusOrderByStartDesc(item.getId(),
                StatusEnum.APPROVED, StatusEnum.WAITING)).thenReturn(List.of(booking));
        when(itemRepository.findAllByOwner_Id(userId, page)).thenReturn(new PageImpl<>(List.of(item)));
        itemDtos = (List<ItemOutDto>) itemService.getAllByUserId(userId, from, size);
        assertNotNull(itemDtos);
        assertEquals(1, itemDtos.size());
        assertEquals(booking.getId(), itemDtos.get(0).getLastBooking().getId());
    }

    @Test
    void findById() {
        long ownerId = owner.getId();
        long itemId = item.getId();
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItem_IdAndStatusOrderByStartDesc(itemId,
                StatusEnum.APPROVED, StatusEnum.WAITING)).thenReturn(List.of(booking));
        when(commentRepository.findAllByItem_Id(itemId, SORT))
                .thenReturn(List.of(comment));
        ItemOutDto itemDto = itemService.getItemById(itemId, ownerId);
        assertNotNull(itemDto);
        assertEquals(itemId, itemDto.getId());
        assertEquals(comment.getId(), itemDto.getComments().get(0).getId());
    }

    @Test
    void findByText() {
        int from = 0;
        int size = 1;

        //EmptyList
        String text = "";
        List<ItemOutDto> itemDtos = (List<ItemOutDto>) itemService.getByText(text, from, size);
        assertNotNull(itemDtos);
        assertEquals(0, itemDtos.size());

        //Regular Case
        text = "дРелЬ";
        when(itemRepository.search(any(), any())).thenReturn(new PageImpl<>(List.of(item)));
        itemDtos = (List<ItemOutDto>) itemService.getByText(text, from, size);
        assertNotNull(itemDtos);
        assertEquals(1, itemDtos.size());
        assertEquals(item.getId(), itemDtos.get(0).getId());
    }

    @Test
    void add() {
        long userId = owner.getId();
        long itemId = item.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any())).thenReturn(item);

        ItemInDto itemDtoToSave = ItemInDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .build();
        ItemOutDto itemDto = itemService.createItem(itemDtoToSave, userId);
        assertNotNull(itemDto);
        assertEquals(itemId, itemDto.getId());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void patch() {
        long userId = owner.getId();
        long itemId = item.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        //fail by empty name
        String parameterName = "Название";
        String error = String.format("%s не может быть пустым", parameterName);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.updateItem(ItemInDto.builder().name("").build(), itemId, userId)
        );
        assertEquals(error, exception.getMessage());

        parameterName = "Описание";
        error = String.format("%s не может быть пустым", parameterName);
        exception = assertThrows(
                ValidationException.class,
                () -> itemService.updateItem(ItemInDto.builder().description("").build(), itemId, userId)
        );
        assertEquals(error, exception.getMessage());

        //regular case
        String newName = "nameUpdate";
        String newDescription = "newDescription";
        item.setName(newName);
        item.setDescription(newDescription);
        when(itemRepository.save(any())).thenReturn(item);
        ItemInDto itemDtoToUpdate = ItemInDto.builder()
                .name(newName)
                .description(newDescription)
                .build();
        ItemOutDto itemDto = itemService.updateItem(itemDtoToUpdate, itemId, userId);
        assertNotNull(itemDto);
        assertEquals("nameUpdate", itemDto.getName());
    }

    @Test
    void delete() {
        long userId = owner.getId();
        long itemId = item.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        doNothing().when(itemRepository).deleteById(itemId);
        itemService.deleteItem(userId, itemId);

        verify(userRepository, times(1)).findById(any());
        verify(itemRepository, times(1)).deleteById(any());

        long notOwnerId = booker.getId();
        String error = String.format("У пользователя с id %d нет вещи с id %d", notOwnerId, itemId);
        when(userRepository.findById(notOwnerId)).thenReturn(Optional.of(booker));
        ObjectNotFoundException exception = assertThrows(
                ObjectNotFoundException.class,
                () -> itemService.deleteItem(notOwnerId, itemId));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void addComment() {
        long userId = booker.getId();
        long itemId = item.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository
                .findByBookerIdAndItemIdAndStatusAndStartIsBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any())).thenReturn(comment);
        CommentDto commentDto = itemService.createComment(CommentDto.builder().text("text").build(), userId, itemId);
        assertNotNull(commentDto);
        assertEquals(comment.getId(), commentDto.getId());

        verify(commentRepository, times(1)).save(any());

        //Fail By not Booker
        long ownerId = owner.getId();
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository
                .findByBookerIdAndItemIdAndStatusAndStartIsBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        String error = "Вы не бронировали вещь. Оставить комментарий невозможно.";
        CommentAccessDeniedException exception = assertThrows(
                CommentAccessDeniedException.class,
                () -> itemService.createComment(CommentDto.builder().text("text").build(), itemId, ownerId)
        );
        assertEquals(error, exception.getMessage());
    }
}
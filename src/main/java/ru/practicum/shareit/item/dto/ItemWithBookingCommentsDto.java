package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Data
@Builder
public class ItemWithBookingCommentsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private List<CommentDto> comments;

    public void addComment(CommentDto comment) {
        comments.add(comment);
    }
}

package ru.practicum.shareit.booking.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.enums.StatusEnum;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "BOOKINGS", schema = "PUBLIC")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "START_DATE", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime start;
    @Column(name = "END_DATE", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime end;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_ID", nullable = false)
    @ToString.Exclude
    private Item item;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOKER_ID", nullable = false)
    @ToString.Exclude
    private User booker;
    @Enumerated(EnumType.STRING)
    private StatusEnum status;
}

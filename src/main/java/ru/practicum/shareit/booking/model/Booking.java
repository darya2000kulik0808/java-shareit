package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.enums.StatusEnum;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "BOOKINGS", schema = "PUBLIC")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "START_DATE", nullable = false)
    private LocalDateTime start;
    @Column(name = "END_DATE", nullable = false)
    private LocalDateTime end;
    @OneToOne
    @JoinColumn(name = "ITEM_ID", nullable = false)
    private Item item;
    @OneToOne
    @JoinColumn(name = "BOOKER_ID", nullable = false)
    private User booker;
    @Enumerated(EnumType.STRING)
    private StatusEnum status;
}

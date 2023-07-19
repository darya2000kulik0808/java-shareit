package ru.practicum.shareit.request.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "REQUESTS", schema = "PUBLIC")
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "DESCRIPTION")
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUESTER_ID")
    @ToString.Exclude
    private User requester;
    @Column(name = "CREATED")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime created;
}

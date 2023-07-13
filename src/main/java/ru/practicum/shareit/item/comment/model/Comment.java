package ru.practicum.shareit.item.comment.model;

import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "COMMENTS", schema = "PUBLIC")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "TEXT")
    private String text;
    @ManyToOne //(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_ID")
    @ToString.Exclude
    private Item item;
    @ManyToOne //(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUTHOR_ID")
    @ToString.Exclude
    private User user;
    @Column(name = "CREATED")
    private LocalDateTime created;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o)
                .getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this)
                .getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Comment comment = (Comment) o;
        return getId() != null && Objects.equals(getId(), comment.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}

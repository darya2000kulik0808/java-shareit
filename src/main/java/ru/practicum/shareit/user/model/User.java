package ru.practicum.shareit.user.model;

import lombok.*;

import javax.persistence.*;


/**
 * TODO Sprint add-controllers.
 */

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "USERS", schema = "PUBLIC")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
}

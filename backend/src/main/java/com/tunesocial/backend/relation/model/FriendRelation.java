package com.tunesocial.backend.relation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "friend_relations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id_1", "user_id_2"})
)
@Getter @Setter
@NoArgsConstructor
public class FriendRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id_1", nullable = false)
    private Long userId1;

    @Column(name = "user_id_2", nullable = false)
    private Long userId2;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public FriendRelation(Long u1, Long u2) {
        if (u1 < u2) {
            this.userId1 = u1;
            this.userId2 = u2;
        } else {
            this.userId1 = u2;
            this.userId2 = u1;
        }
        this.createdAt = Instant.now();
    }
}

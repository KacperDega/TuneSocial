package com.tunesocial.backend.post.model;

import com.tunesocial.backend.post.model.enums.ReactionTargetType;
import com.tunesocial.backend.post.model.enums.ReactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "reactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "targetId", "targetType"})
})
@Getter @Setter
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionTargetType targetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

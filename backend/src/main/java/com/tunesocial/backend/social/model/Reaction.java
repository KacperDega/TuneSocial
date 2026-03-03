package com.tunesocial.backend.social.model;

import com.tunesocial.backend.social.model.enums.ReactionTargetType;
import com.tunesocial.backend.social.model.enums.ReactionType;
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

    private Long userId;

    private Long targetId;

    @Enumerated(EnumType.STRING)
    private ReactionTargetType targetType;

    @Enumerated(EnumType.STRING)
    private ReactionType type;

    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

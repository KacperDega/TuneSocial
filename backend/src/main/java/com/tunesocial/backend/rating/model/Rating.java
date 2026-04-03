package com.tunesocial.backend.rating.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Setter
@Getter
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "target_id", "target_type"}
        ),
        indexes = {
                @Index(
                        name = "idx_rating_target_idtype",
                        columnList = "target_id, target_type"
                )
        }
)
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RatingTargetType targetType;

    @Column(nullable = false)
    private int ratingValue;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
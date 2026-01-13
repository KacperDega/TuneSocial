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
                ),
                @Index(
                        name = "idx_rating_userid",
                        columnList = "user_id"
                )
        }
)
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String targetId;

    @Enumerated(EnumType.STRING)
    private RatingTargetType targetType;

    private int value;

    private Instant createdAt;

}
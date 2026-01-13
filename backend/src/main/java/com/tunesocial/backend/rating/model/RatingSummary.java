package com.tunesocial.backend.rating.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"target_id", "target_type"}
        )
)
public class RatingSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String targetId;

    @Enumerated(EnumType.STRING)
    private RatingTargetType targetType;

    private long ratingCount;

    private long ratingSum;


    public double getAverage() {
        if (ratingCount == 0) return 0.0;
        return (double) ratingSum / ratingCount;
    }
}
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

    @Column(nullable = false)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RatingTargetType targetType;

    @Column(nullable = false)
    private long ratingCount;

    @Column(nullable = false)
    private long ratingSum;

    public double getAverage() {
        if (ratingCount == 0) return 0.0;
        return (double) ratingSum / ratingCount;
    }
}
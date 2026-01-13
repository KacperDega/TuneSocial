package com.tunesocial.backend.rating.repository;

import com.tunesocial.backend.rating.model.RatingSummary;
import com.tunesocial.backend.rating.model.RatingTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RatingSummaryRepository extends JpaRepository<RatingSummary, Long> {

    Optional<RatingSummary> findByTargetIdAndTargetType(String targetId, RatingTargetType targetType);

    @Modifying
    @Query("""
        UPDATE RatingSummary rs
        SET rs.ratingCount = rs.ratingCount + :countDiff,
            rs.ratingSum = rs.ratingSum + :sumDiff
        WHERE rs.targetId = :targetId AND rs.targetType = :targetType
    """)
    int updateSummary(String targetId, RatingTargetType targetType, long countDiff, long sumDiff);

}

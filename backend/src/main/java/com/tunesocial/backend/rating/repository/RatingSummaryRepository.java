package com.tunesocial.backend.rating.repository;

import com.tunesocial.backend.music.dto.TrackDetailsResponse;
import com.tunesocial.backend.rating.model.RatingSummary;
import com.tunesocial.backend.rating.model.RatingTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("""
        SELECT s FROM RatingSummary s
        WHERE s.targetType = :type AND s.ratingCount >= :minVotes
        ORDER BY ((s.ratingSum + :m * :C) / (s.ratingCount + :m)) DESC
    """)
    Page<RatingSummary> findTopSummaries(
            @Param("type") RatingTargetType type,
            @Param("m") long m,
            @Param("C") double C,
            @Param("minVotes") long minVotes,
            Pageable pageable);


    @Query("""
        SELECT AVG(CAST(s.ratingSum AS double) / s.ratingCount)
        FROM RatingSummary s
        WHERE s.targetType = :type
    """)
    Double getGlobalAverage(RatingTargetType type);
}

package com.tunesocial.backend.rating.repository;

import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndTargetIdAndTargetType(
            Long userId,
            String targetId,
            RatingTargetType targetType
    );

    Page<Rating> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Rating> findAllByTargetIdAndTargetTypeAndCommentIsNotNull(
            String targetId,
            RatingTargetType targetType,
            Pageable pageable
    );

    Page<Rating> findAllByUserIdAndCommentIsNotNullAndCommentIsNotEmpty(
            Long userId,
            Pageable pageable);

    Page<Rating> findAllByUserIdAndTargetTypeAndCommentIsNotNullAndCommentIsNotEmpty(
            Long userId,
            RatingTargetType targetType,
            Pageable pageable
    );

    Page<Rating> findByCommentIsNotNullAndCommentNotEmptyOrderByCreatedAtDesc(Pageable pageable);
}
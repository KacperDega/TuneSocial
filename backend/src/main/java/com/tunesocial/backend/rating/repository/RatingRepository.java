package com.tunesocial.backend.rating.repository;

import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndTargetIdAndTargetType(
            Long userId,
            String targetId,
            RatingTargetType targetType
    );

    List<Rating> findAllByUserId(Long userId);
}
package com.tunesocial.backend.social.repository;

import com.tunesocial.backend.social.model.Reaction;
import com.tunesocial.backend.social.model.enums.ReactionTargetType;
import com.tunesocial.backend.social.model.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, ReactionTargetType targetType);


    interface ReactionCount {
        ReactionType getType();
        Long getCount();
    }

    @Query("SELECT r.type as type, COUNT(r) as count " +
            "FROM Reaction r WHERE r.targetId = :targetId AND r.targetType = :targetType " +
            "GROUP BY r.type")
    List<ReactionCount> countByTarget(Long targetId, ReactionTargetType targetType);
}

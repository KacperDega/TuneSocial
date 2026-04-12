package com.tunesocial.backend.post.repository;

import com.tunesocial.backend.post.model.Reaction;
import com.tunesocial.backend.post.model.enums.ReactionTargetType;
import com.tunesocial.backend.post.model.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    @Modifying
    @Query("DELETE FROM Reaction r WHERE r.targetType = :targetType AND r.targetId IN :targetIds")
    void deleteAllByTargetTypeAndTargetIdIn(
            @Param("targetType") ReactionTargetType targetType,
            @Param("targetIds") Collection<Long> targetIds
    );
}

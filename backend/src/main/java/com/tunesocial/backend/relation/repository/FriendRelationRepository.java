package com.tunesocial.backend.relation.repository;

import com.tunesocial.backend.relation.model.FriendRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRelationRepository extends JpaRepository<FriendRelation,Long> {

    @Query("SELECT CASE " +
                "WHEN fr.userId1 = :userId THEN fr.userId2 " +
                "ELSE fr.userId1 END " +
            "FROM FriendRelation fr WHERE fr.userId1 = :userId OR fr.userId2 = :userId " +
            "ORDER BY fr.createdAt DESC")
    Page<Long> findAllFriendIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(fr) FROM FriendRelation fr WHERE fr.userId1 = :userId OR fr.userId2 = :userId")
    Integer countByUserId(Long userId);

    @Query("SELECT COUNT(fr) > 0 FROM FriendRelation fr " +
            "WHERE (fr.userId1 = :u1 AND fr.userId2 = :u2) OR (fr.userId1 = :u2 AND fr.userId2 = :u1)")
    boolean areFriends(@Param("u1") Long u1, @Param("u2") Long u2);

//    @Query("SELECT fr FROM FriendRelation fr WHERE (fr.userId1 = :u1 AND fr.userId2 = :u2) OR (fr.userId1 = :u2 AND fr.userId2 = :u1)")
    Optional<FriendRelation> findFriendRelationByUserId1AndUserId2(@Param("u1") Long u1, @Param("u2") Long u2);
}

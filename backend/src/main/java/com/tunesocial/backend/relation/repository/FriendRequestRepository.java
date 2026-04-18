package com.tunesocial.backend.relation.repository;

import com.tunesocial.backend.relation.model.FriendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findByRequesterIdAndRecipientId(Long requesterId, Long recipientId);

    Page<FriendRequest> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    boolean existsByRequesterIdAndRecipientId(Long requesterId, Long recipientId);
}

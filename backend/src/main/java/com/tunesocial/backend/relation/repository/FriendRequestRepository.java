package com.tunesocial.backend.relation.repository;

import com.tunesocial.backend.relation.model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findByRequesterIdAndRecipientId(Long requesterId, Long recipientId);

    List<FriendRequest> findByRecipientId(Long recipientId);

    boolean existsByRequesterIdAndRecipientId(Long requesterId, Long recipientId);
}

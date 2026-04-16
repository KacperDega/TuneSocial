package com.tunesocial.backend.relation.service;

import com.tunesocial.backend.relation.model.FriendRelation;
import com.tunesocial.backend.relation.model.FriendRequest;
import com.tunesocial.backend.relation.repository.FriendRelationRepository;
import com.tunesocial.backend.relation.repository.FriendRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendRelationRepository friendRelationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FollowService followService;

    // TODO: EXCEPTION
    @Transactional
    public void sendFriendRequest(Long requesterId, Long recipientId) {
        if (requesterId.equals(recipientId)) {
            throw new RuntimeException("Cannot send friend request to yourself");
        }

        if (friendRelationRepository.areFriends(requesterId, recipientId)) {
            throw new RuntimeException("Users are already friends");
        }

        if (friendRequestRepository.existsByRequesterIdAndRecipientId(requesterId, recipientId)) {
            throw new RuntimeException("Friend request already sent");
        }

        Optional<FriendRequest> reverseRequest = friendRequestRepository.findByRequesterIdAndRecipientId(recipientId, requesterId);
        if (reverseRequest.isPresent()) {
            acceptFriendRequest(requesterId, reverseRequest.get().getId());
            return;
        }

        FriendRequest request = new FriendRequest(requesterId, recipientId);
        friendRequestRepository.save(request);

        // eventPublisher.publishEvent(new FriendRequestSentEvent(requesterId, recipientId));
    }

    // TODO: EXCEPTION
    @Transactional
    public void acceptFriendRequest(Long currentUserId, Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!request.getRecipientId().equals(currentUserId)) {
            // TODO: REMOVE THE REQUEST
            throw new RuntimeException("Not authorized to accept this friend request");
        }

        FriendRelation friendship = new FriendRelation(request.getRequesterId(), request.getRecipientId());
        friendRelationRepository.save(friendship);

        followService.ensureFollow(request.getRequesterId(), request.getRecipientId());
        followService.ensureFollow(request.getRecipientId(), request.getRequesterId());

        friendRequestRepository.delete(request);

        // eventPublisher.publishEvent(new FriendRequestAcceptedEvent(currentUserId, request.getRequesterId()));
    }

    // TODO: EXCEPTION
    @Transactional
    public void cancelOrRejectFriendRequest(Long currentUserId, Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        boolean isRecipient = request.getRecipientId().equals(currentUserId);
        boolean isRequester = request.getRequesterId().equals(currentUserId);

        if (!isRecipient && !isRequester) {
            throw new RuntimeException("Not authorized to modify this friend request");
        }

        friendRequestRepository.delete(request);
    }

    @Transactional
    public void removeFriend(Long currentUserId, Long friendId) {
        Long u1 = Math.min(currentUserId, friendId);
        Long u2 = Math.max(currentUserId, friendId);

        friendRelationRepository.findFriendRelationByUserId1AndUserId2(u1, u2)
                .ifPresent(friendRelationRepository::delete);
    }

    @Transactional(readOnly = true)
    public long getFriendCount(Long userId) {
        return friendRelationRepository.countByUserId(userId);
    }
}

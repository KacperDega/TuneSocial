package com.tunesocial.backend.relation.service;

import com.tunesocial.backend.relation.dto.FriendRequestDto;
import com.tunesocial.backend.relation.mapper.FriendRequestMapper;
import com.tunesocial.backend.relation.model.FriendRelation;
import com.tunesocial.backend.relation.model.FriendRequest;
import com.tunesocial.backend.relation.repository.FriendRelationRepository;
import com.tunesocial.backend.relation.repository.FriendRequestRepository;
import com.tunesocial.backend.user.dto.UserRefDto;
import com.tunesocial.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendRelationRepository friendRelationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FollowService followService;
    private final UserService userService;
    private final FriendRequestMapper friendRequestMapper;

    @Transactional(readOnly = true)
    public List<FriendRequestDto> getFriendRequests(Long recipientId) {
        List<FriendRequest> requests = friendRequestRepository.findByRecipientId(recipientId);
        if (requests.isEmpty()) {return new ArrayList<>();}

        Set<Long> requesterIds = requests.stream().map(FriendRequest::getRequesterId).collect(Collectors.toSet());

        Map<Long, UserRefDto> requestersDetails = userService.getUserReferencesByIds(requesterIds);

        return friendRequestMapper.toDtoList(requests, requestersDetails);
    }

    @Transactional(readOnly = true)
    public Page<UserRefDto> getUserFriends(Long userId, Pageable pageable) {
        Page<Long> friendIdsPage = friendRelationRepository.findAllFriendIdsByUserId(userId, pageable);

        if (friendIdsPage.isEmpty()) {
            return Page.empty(pageable);
        }

        Set<Long> friendIds = new HashSet<>(friendIdsPage.getContent());
        Map<Long, UserRefDto> userRefs = userService.getUserReferencesByIds(friendIds);

        return friendIdsPage.map(id -> userRefs.getOrDefault(
                id,
                new UserRefDto(id, null, "User_" + id, 1)
        ));
    }

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

package com.tunesocial.backend.relation.service;

import com.tunesocial.backend.relation.model.FollowRelation;
import com.tunesocial.backend.relation.repository.FollowRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRelationRepository followRelationRepository;
    private final ApplicationEventPublisher eventPublisher;

    // TODO: EXCEPTION
    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("Cannot follow yourself");
        }

        if (followRelationRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            return;
        }

        FollowRelation follow = new FollowRelation(followerId, followingId);
        followRelationRepository.save(follow);

//        eventPublisher.publishEvent(new UserFollowedEvent(followerId, followingId));
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        followRelationRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .ifPresent(followRelationRepository::delete);
    }

    // TODO: EXCEPTION
    @Transactional
    public void ensureFollow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("Cannot follow yourself");
        }

        if (!followRelationRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            followRelationRepository.save(new FollowRelation(followerId, followingId));
        }
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followingId) {
        return followRelationRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Transactional(readOnly = true)
    public long getFollowersCount(Long userId) {
        return followRelationRepository.countByFollowingId(userId);
    }

    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        return followRelationRepository.countByFollowerId(userId);
    }
}

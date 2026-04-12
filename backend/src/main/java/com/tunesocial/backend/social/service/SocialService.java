package com.tunesocial.backend.social.service;

import com.tunesocial.backend.social.dto.ReactionsSummary;
import com.tunesocial.backend.social.event.ReactionAddedEvent;
import com.tunesocial.backend.social.model.FeedItem;
import com.tunesocial.backend.social.model.Reaction;
import com.tunesocial.backend.social.model.enums.FeedItemType;
import com.tunesocial.backend.social.model.enums.ReactionTargetType;
import com.tunesocial.backend.social.model.enums.ReactionType;
import com.tunesocial.backend.social.repository.FeedItemRepository;
import com.tunesocial.backend.social.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialService {
    private final FeedItemRepository feedItemRepository;
    private final ReactionRepository reactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createPost(FeedItemType type, String referenceId, Long userId) {
        FeedItem item = new FeedItem();
        item.setType(type);
        item.setReferenceId(referenceId);
        item.setUserId(userId);
        feedItemRepository.save(item);
    }

    @Transactional
    public void toggleReaction(Long userId, Long targetId, ReactionTargetType targetType, ReactionType reactionType) {
        Optional<Reaction> existing = reactionRepository
                .findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);

        boolean notify = false;

        if (existing.isPresent()) {
            Reaction reaction = existing.get();
            if (reaction.getType() == reactionType) {
                // same as existing - delete
                reactionRepository.delete(reaction);
            } else {
                // another - update
                reaction.setType(reactionType);
                reactionRepository.save(reaction);
                notify = true;
            }
        } else {
            Reaction newReaction = new Reaction();
            newReaction.setUserId(userId);
            newReaction.setTargetId(targetId);
            newReaction.setTargetType(targetType);
            newReaction.setType(reactionType);
            reactionRepository.save(newReaction);
            notify = true;
        }

        if (notify) {
            eventPublisher.publishEvent(new ReactionAddedEvent(
                    userId,
                    targetType,
                    targetId.toString()
            ));
        }
    }

    @Transactional
    public void removeReactionsForTargets(ReactionTargetType targetType, Collection<Long> targetIds) {
        if (targetIds != null && !targetIds.isEmpty()) {
            reactionRepository.deleteAllByTargetTypeAndTargetIdIn(targetType, targetIds);
        }
    }

    @Transactional(readOnly = true)
    public ReactionsSummary getReactionSummary(Long targetId, ReactionTargetType targetType, Long currentUserId) {
        List<ReactionRepository.ReactionCount> counts = reactionRepository.countByTarget(targetId, targetType);

        Map<ReactionType, Long> countsByType = counts.stream()
                .collect(Collectors.toMap(
                        ReactionRepository.ReactionCount::getType,
                        ReactionRepository.ReactionCount::getCount)
                );

        long totalCount = countsByType.values().stream().mapToLong(Long::longValue).sum();

        ReactionType myReaction = null;
        if (currentUserId != null) {
            myReaction = reactionRepository.findByUserIdAndTargetIdAndTargetType(currentUserId, targetId, targetType)
                    .map(Reaction::getType)
                    .orElse(null);
        }

        return new ReactionsSummary(totalCount, countsByType, myReaction);
    }
}

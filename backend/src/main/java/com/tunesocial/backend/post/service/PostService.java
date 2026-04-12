package com.tunesocial.backend.post.service;

import com.tunesocial.backend.post.model.FeedItem;
import com.tunesocial.backend.post.model.enums.FeedItemType;
import com.tunesocial.backend.post.repository.FeedItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final FeedItemRepository feedItemRepository;

    @Transactional
    public void createPost(FeedItemType type, String referenceId, Long userId) {
        FeedItem item = new FeedItem();
        item.setType(type);
        item.setReferenceId(referenceId);
        item.setUserId(userId);
        feedItemRepository.save(item);
    }
}

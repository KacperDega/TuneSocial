package com.tunesocial.backend.post.repository;

import com.tunesocial.backend.post.model.FeedItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedItemRepository extends JpaRepository<FeedItem, Long> {
}

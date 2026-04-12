package com.tunesocial.backend.post.model;

import com.tunesocial.backend.post.model.enums.FeedItemType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "feed_items")
@Getter @Setter
public class FeedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedItemType type;

    @Column()
    private String referenceId;

    @Column(length = 2000)
    private String content;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

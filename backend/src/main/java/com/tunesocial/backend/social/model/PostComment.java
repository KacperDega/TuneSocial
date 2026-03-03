package com.tunesocial.backend.social.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "post_comments")
public class PostComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId; // Referencja do FeedItem
    private Long userId;

    @Column(length = 500)
    private String content;

    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }
}

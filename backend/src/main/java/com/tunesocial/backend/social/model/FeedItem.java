package com.tunesocial.backend.social.model;

import com.tunesocial.backend.social.model.enums.FeedItemType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "feed_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "type", "referenceId"} )
})
@Getter @Setter
public class FeedItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private FeedItemType type;

    private String referenceId;

    private Long userId;

    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

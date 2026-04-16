package com.tunesocial.backend.relation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "friend_requests",
        uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "recipient_id"})
)
@Getter @Setter
@NoArgsConstructor
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public FriendRequest(Long requesterId, Long recipientId) {
        this.requesterId = requesterId;
        this.recipientId = recipientId;
    }
}

package com.tunesocial.backend.user.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users")
@Setter
@Getter
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username; // @username

    @JsonIgnore
    @Column(nullable = false)
    private String passwordHash;

    Instant createdAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}


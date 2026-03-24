package com.tunesocial.backend.user.model;

import com.tunesocial.backend.user.model.enums.BirthDateVisibility;
import com.tunesocial.backend.user.model.enums.ProfileVisibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
@Getter @Setter
public class UserProfile {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String displayName;

    @Column(length = 500)
    private String bio;

    private Integer avatarId = 1;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BirthDateVisibility birthDateVisibility = BirthDateVisibility.YEAR_ONLY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileVisibility profileVisibility = ProfileVisibility.PUBLIC;

    @Column(nullable = false)
    private boolean isSetup = false;

    private Instant updatedAt;
    private Instant createdAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}

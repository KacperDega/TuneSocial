package com.tunesocial.backend.user.repository;

import com.tunesocial.backend.user.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}

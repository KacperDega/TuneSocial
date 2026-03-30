package com.tunesocial.backend.user.repository;

import com.tunesocial.backend.user.dto.UserRefDto;
import com.tunesocial.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("""
        SELECT new com.tunesocial.backend.user.dto.UserRefDto(
            u.id,
            u.username,
            p.displayName,
            p.avatarId
        )
        FROM User u
        JOIN UserProfile p ON u.id = p.id
        WHERE u.id IN :userIds
    """)
    List<UserRefDto> findUserRefsByIds(@Param("userIds") Set<Long> userIds);
}


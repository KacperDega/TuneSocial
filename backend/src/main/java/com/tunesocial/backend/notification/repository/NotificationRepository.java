package com.tunesocial.backend.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.tunesocial.backend.notification.model.Notification;
import com.tunesocial.backend.notification.model.enums.NotificationTargetType;
import com.tunesocial.backend.notification.model.enums.NotificationType;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByUserIdAndTargetTypeAndTargetIdAndType(
            Long userId,
            NotificationTargetType targetType,
            String targetId,
            NotificationType type
    );

    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadForUser(@Param("userId") Long userId);


    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.id NOT IN " +
            "(SELECT n2.id FROM Notification n2 WHERE n2.userId = :userId ORDER BY n2.createdAt DESC LIMIT :maxToKeep)")
    void trimOldNotificationsForUser(@Param("userId") Long userId, @Param("maxToKeep") int maxToKeep);
}

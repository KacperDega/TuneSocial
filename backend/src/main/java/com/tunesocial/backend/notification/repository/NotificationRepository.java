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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByUserIdAndTargetTypeAndTargetIdAndType(
            Long userId,
            NotificationTargetType targetType,
            String targetId,
            NotificationType type
    );

    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.context WHERE n.userId = :userId ORDER BY n.createdAt DESC",
            countQuery = "SELECT count(n) FROM Notification n WHERE n.userId = :userId")
    Page<Notification> findAllByUserIdWithContextOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadForUser(@Param("userId") Long userId);


    @Query(value = "SELECT n.id FROM Notification n WHERE n.userId = :userId " +
            "ORDER BY n.createdAt DESC OFFSET :maxToKeep", nativeQuery = false)
    List<Long> findOldNotificationIdsToTrim(@Param("userId") Long userId, @Param("maxToKeep") int maxToKeep);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.id IN :ids")
    void deleteNotificationsByIds(@Param("ids") Collection<Long> ids);

    @Modifying
    @Query("DELETE FROM NotificationContextData c WHERE c.notification.id IN :ids")
    void deleteContextsByNotificationIds(@Param("ids") Collection<Long> ids);
}

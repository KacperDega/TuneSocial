package com.tunesocial.backend.notification.exception;

public class NotificationAccessDeniedException extends RuntimeException {
    public NotificationAccessDeniedException(Long notificationId) {
        super("You do not have permission to access this notification (ID: " + notificationId + ")");
    }
}
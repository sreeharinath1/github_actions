package com.projecta.notification.repository;

import com.projecta.notification.model.Notification;
import com.projecta.notification.model.NotificationStatus;
import com.projecta.notification.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientId(String recipientId, Pageable pageable);

    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);

    List<Notification> findByReferenceIdAndReferenceType(String referenceId, String referenceType);

    long countByRecipientIdAndTypeAndCreatedAtAfter(
            String recipientId, NotificationType type, LocalDateTime after);
}

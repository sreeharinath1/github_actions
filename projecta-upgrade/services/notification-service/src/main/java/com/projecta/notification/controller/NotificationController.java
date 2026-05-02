package com.projecta.notification.controller;

import com.projecta.notification.dto.NotificationEvent;
import com.projecta.notification.model.Notification;
import com.projecta.notification.repository.NotificationRepository;
import com.projecta.notification.service.NotificationDispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationDispatchService dispatchService;

    /** Get my notifications (paginated) */
    @GetMapping
    public ResponseEntity<Page<Notification>> getMyNotifications(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(notificationRepository.findByRecipientId(userId, pageable));
    }

    /** Get notifications for a specific reference (e.g., order) */
    @GetMapping("/reference/{referenceType}/{referenceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Notification>> getByReference(
            @PathVariable String referenceType,
            @PathVariable String referenceId) {
        return ResponseEntity.ok(
                notificationRepository.findByReferenceIdAndReferenceType(referenceId, referenceType)
        );
    }

    /** Admin: manually trigger a notification */
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationEvent event) {
        dispatchService.dispatch(event);
        return ResponseEntity.accepted().build();
    }

    /** Health check */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is UP");
    }
}

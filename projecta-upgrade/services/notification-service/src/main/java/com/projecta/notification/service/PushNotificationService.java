package com.projecta.notification.service;

import com.google.firebase.messaging.*;
import com.projecta.notification.dto.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushNotificationService {

    public void send(NotificationEvent event) {
        if (event.getDeviceToken() == null || event.getDeviceToken().isBlank()) {
            log.warn("No device token for PUSH eventId={}", event.getEventId());
            return;
        }

        try {
            com.google.firebase.messaging.Message fcmMessage = com.google.firebase.messaging.Message.builder()
                    .setToken(event.getDeviceToken())
                    .setNotification(Notification.builder()
                            .setTitle(event.getSubject())
                            .setBody(event.getBody())
                            .build())
                    .putData("type", event.getType().name())
                    .putData("referenceId", event.getReferenceId() != null ? event.getReferenceId() : "")
                    .putData("referenceType", event.getReferenceType() != null ? event.getReferenceType() : "")
                    .build();

            String response = FirebaseMessaging.getInstance().send(fcmMessage);
            log.info("Push notification sent: messageId={} for eventId={}", response, event.getEventId());

        } catch (FirebaseMessagingException e) {
            log.error("Push send failed for eventId={}: {}", event.getEventId(), e.getMessage());
            throw new RuntimeException("Push send failed: " + e.getMessage(), e);
        }
    }
}

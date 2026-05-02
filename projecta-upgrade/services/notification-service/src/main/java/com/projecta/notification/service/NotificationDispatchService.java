package com.projecta.notification.service;

import com.projecta.notification.dto.NotificationEvent;
import com.projecta.notification.model.*;
import com.projecta.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final PushNotificationService pushService;

    @Transactional
    public void dispatch(NotificationEvent event) {
        List<NotificationChannel> channels = event.getChannels();
        if (channels == null || channels.isEmpty()) {
            channels = List.of(NotificationChannel.EMAIL); // default
        }

        for (NotificationChannel channel : channels) {
            Notification notification = buildNotification(event, channel);
            notificationRepository.save(notification);

            try {
                switch (channel) {
                    case EMAIL -> emailService.send(event);
                    case SMS -> smsService.send(event);
                    case PUSH -> pushService.send(event);
                    case IN_APP -> log.info("IN_APP notification stored for user {}", event.getRecipientId());
                }

                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);

            } catch (Exception e) {
                log.error("Failed to send {} notification for eventId={}: {}",
                        channel, event.getEventId(), e.getMessage());
                notification.setStatus(NotificationStatus.FAILED);
                notification.setErrorMessage(e.getMessage());
                notificationRepository.save(notification);
                throw e; // propagate for retry
            }
        }
    }

    @Transactional
    public void markFailed(NotificationEvent event, String reason) {
        Notification notification = buildNotification(event, NotificationChannel.EMAIL);
        notification.setStatus(NotificationStatus.FAILED);
        notification.setErrorMessage(reason);
        notificationRepository.save(notification);
    }

    private Notification buildNotification(NotificationEvent event, NotificationChannel channel) {
        return Notification.builder()
                .recipientId(event.getRecipientId())
                .recipientEmail(event.getRecipientEmail())
                .recipientPhone(event.getRecipientPhone())
                .type(event.getType())
                .channel(channel)
                .status(NotificationStatus.PENDING)
                .subject(event.getSubject())
                .body(event.getBody())
                .templateId(event.getTemplateId())
                .referenceId(event.getReferenceId())
                .referenceType(event.getReferenceType())
                .build();
    }
}

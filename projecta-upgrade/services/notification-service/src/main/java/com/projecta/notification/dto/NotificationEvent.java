package com.projecta.notification.dto;

import com.projecta.notification.model.NotificationChannel;
import com.projecta.notification.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationEvent {

    private String eventId;
    private String recipientId;
    private String recipientEmail;
    private String recipientPhone;
    private String deviceToken;       // For push notifications

    private NotificationType type;
    private List<NotificationChannel> channels;  // Send via multiple channels

    private String subject;
    private String body;
    private String templateId;
    private Map<String, String> templateVars;   // e.g. {orderId: "123", amount: "$50"}

    private String referenceId;
    private String referenceType;
}

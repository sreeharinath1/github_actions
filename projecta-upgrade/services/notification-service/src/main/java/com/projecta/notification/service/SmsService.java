package com.projecta.notification.service;

import com.projecta.notification.dto.NotificationEvent;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void send(NotificationEvent event) {
        if (event.getRecipientPhone() == null || event.getRecipientPhone().isBlank()) {
            log.warn("No phone number for SMS eventId={}", event.getEventId());
            return;
        }

        try {
            String smsBody = truncate(event.getBody(), 160);

            Message message = Message.creator(
                    new PhoneNumber(event.getRecipientPhone()),
                    new PhoneNumber(fromNumber),
                    smsBody
            ).create();

            log.info("SMS sent to {} (sid={}) for eventId={}",
                    event.getRecipientPhone(), message.getSid(), event.getEventId());

        } catch (Exception e) {
            log.error("SMS send failed to {} for eventId={}: {}",
                    event.getRecipientPhone(), event.getEventId(), e.getMessage());
            throw new RuntimeException("SMS send failed: " + e.getMessage(), e);
        }
    }

    private String truncate(String text, int maxLen) {
        return (text != null && text.length() > maxLen) ? text.substring(0, maxLen - 3) + "..." : text;
    }
}

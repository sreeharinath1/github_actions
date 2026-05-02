package com.projecta.notification.kafka;

import com.projecta.notification.dto.NotificationEvent;
import com.projecta.notification.service.NotificationDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaConsumer {

    private final NotificationDispatchService dispatchService;

    @KafkaListener(
        topics = {
            "order.placed",
            "order.confirmed",
            "order.shipped",
            "order.delivered",
            "order.cancelled",
            "payment.success",
            "payment.failed",
            "payment.refunded",
            "user.registered",
            "user.password-reset"
        },
        groupId = "notification-service-group",
        containerFactory = "notificationKafkaListenerFactory"
    )
    public void onNotificationEvent(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received notification event: topic={}, partition={}, offset={}, eventId={}, type={}",
                topic, partition, offset, event.getEventId(), event.getType());

        try {
            dispatchService.dispatch(event);
            acknowledgment.acknowledge();
            log.info("Notification dispatched successfully for eventId={}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to dispatch notification for eventId={}: {}", event.getEventId(), e.getMessage(), e);
            // Do NOT acknowledge — message will be retried based on retry config
            // Dead letter topic handles exhausted retries
        }
    }
}

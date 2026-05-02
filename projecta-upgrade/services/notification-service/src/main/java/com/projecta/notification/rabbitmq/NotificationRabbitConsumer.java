package com.projecta.notification.rabbitmq;

import com.projecta.notification.dto.NotificationEvent;
import com.projecta.notification.service.NotificationDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRabbitConsumer {

    private final NotificationDispatchService dispatchService;

    /**
     * Payment service publishes to RabbitMQ for guaranteed delivery of
     * financial notification events (payment success/failure).
     */
    @RabbitListener(queues = "${rabbitmq.queues.payment-notifications}")
    public void onPaymentNotification(NotificationEvent event) {
        log.info("RabbitMQ: received payment notification event: eventId={}, type={}",
                event.getEventId(), event.getType());
        try {
            dispatchService.dispatch(event);
        } catch (Exception e) {
            log.error("RabbitMQ: failed to process payment notification eventId={}: {}",
                    event.getEventId(), e.getMessage(), e);
            throw e; // Re-throw triggers RabbitMQ retry/DLQ
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.notification-dlq}")
    public void onDeadLetter(NotificationEvent event) {
        log.error("DLQ: unprocessable notification event: eventId={}, type={}",
                event.getEventId(), event.getType());
        // Persist DLQ event for manual review / alerting
        dispatchService.markFailed(event, "Exhausted retries - moved to DLQ");
    }
}

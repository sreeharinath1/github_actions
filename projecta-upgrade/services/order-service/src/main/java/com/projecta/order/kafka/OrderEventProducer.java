package com.projecta.order.kafka;

import com.projecta.notification.dto.NotificationEvent;
import com.projecta.order.model.Order;
import com.projecta.notification.model.NotificationChannel;
import com.projecta.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderPlaced(Order order) {
        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .recipientId(order.getUserId())
                .recipientEmail(order.getUserEmail())
                .recipientPhone(order.getUserPhone())
                .type(NotificationType.ORDER_PLACED)
                .channels(List.of(NotificationChannel.EMAIL, NotificationChannel.SMS))
                .subject("Order Confirmed - #" + order.getId())
                .body("Your order #" + order.getId() + " has been placed successfully.")
                .templateId("order-placed")
                .templateVars(Map.of(
                        "orderId", order.getId().toString(),
                        "amount", order.getTotalAmount().toString(),
                        "userName", order.getUserName()
                ))
                .referenceId(order.getId().toString())
                .referenceType("ORDER")
                .build();

        sendEvent("order.placed", order.getId().toString(), event);
    }

    public void publishOrderShipped(Order order, String trackingNumber) {
        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .recipientId(order.getUserId())
                .recipientEmail(order.getUserEmail())
                .type(NotificationType.ORDER_SHIPPED)
                .channels(List.of(NotificationChannel.EMAIL, NotificationChannel.PUSH))
                .subject("Your Order is on the Way! 🚚")
                .templateId("order-shipped")
                .templateVars(Map.of(
                        "orderId", order.getId().toString(),
                        "trackingNumber", trackingNumber
                ))
                .referenceId(order.getId().toString())
                .referenceType("ORDER")
                .build();

        sendEvent("order.shipped", order.getId().toString(), event);
    }

    public void publishOrderCancelled(Order order, String reason) {
        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .recipientId(order.getUserId())
                .recipientEmail(order.getUserEmail())
                .type(NotificationType.ORDER_CANCELLED)
                .channels(List.of(NotificationChannel.EMAIL))
                .subject("Order #" + order.getId() + " Cancelled")
                .templateId("order-cancelled")
                .templateVars(Map.of(
                        "orderId", order.getId().toString(),
                        "reason", reason
                ))
                .referenceId(order.getId().toString())
                .referenceType("ORDER")
                .build();

        sendEvent("order.cancelled", order.getId().toString(), event);
    }

    private void sendEvent(String topic, String key, Object payload) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, payload);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic={} key={}: {}", topic, key, ex.getMessage());
            } else {
                log.info("Event published: topic={}, partition={}, offset={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}

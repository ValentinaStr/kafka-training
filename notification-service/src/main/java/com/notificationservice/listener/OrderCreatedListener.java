package com.notificationservice.listener;

import com.notificationservice.dto.OrderCreatedEvent;
import com.notificationservice.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final EmailNotificationService emailNotificationService;

    @KafkaListener(topics = "${app.kafka.topics.order-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderCreated(OrderCreatedEvent event) {
        emailNotificationService.sendOrderConfirmation(event);
    }
}

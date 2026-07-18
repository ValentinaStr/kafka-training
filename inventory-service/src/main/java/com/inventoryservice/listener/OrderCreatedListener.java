package com.inventoryservice.listener;

import com.inventoryservice.dto.OrderCreatedEvent;
import com.inventoryservice.service.OrderProcessingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

    private final OrderProcessingService orderProcessingService;

    public OrderCreatedListener(OrderProcessingService orderProcessingService) {
        this.orderProcessingService = orderProcessingService;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderCreated(OrderCreatedEvent event) {
        orderProcessingService.process(event);
    }
}

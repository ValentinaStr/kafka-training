package com.orderservice.listener;

import com.orderservice.dto.InventoryResult;
import com.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryResultListener {

    private final OrderService orderService;

    @KafkaListener(topics = "${app.kafka.topics.inventory-result}", groupId = "${spring.kafka.consumer.group-id}")
    public void onInventoryResult(InventoryResult result) {
        orderService.updateOrderStatus(result);
    }
}

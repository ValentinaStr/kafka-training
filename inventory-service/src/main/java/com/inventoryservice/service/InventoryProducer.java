package com.inventoryservice.service;

import com.inventoryservice.dto.InventoryResultEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public InventoryProducer(KafkaTemplate<String, Object> kafkaTemplate,
                              @Value("${app.kafka.topics.inventory-result}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void send(InventoryResultEvent event) {
        log.debug("Sending InventoryResult event for order {} to topic {}", event.orderId(), topic);
        kafkaTemplate.send(topic, event.orderId().toString(), event);
    }
}

package com.orderservice.service;

import com.orderservice.dto.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public OrderProducer(KafkaTemplate<String, Object> kafkaTemplate,
                          @Value("${app.kafka.topics.order-created}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void send(OrderCreatedEvent event) {
        kafkaTemplate.send(topic, event);
    }
}
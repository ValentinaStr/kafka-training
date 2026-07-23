package com.orderservice.service;

import com.orderservice.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public OrderProducer(KafkaTemplate<String, Object> kafkaTemplate,
                          @Value("${app.kafka.topics.order-created}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @TransactionalEventListener
    // Default phase is AFTER_COMMIT.
    // Other phases: BEFORE_COMMIT, AFTER_ROLLBACK, AFTER_COMPLETION.
    public void send(OrderCreatedEvent event) {
        log.debug("Sending OrderCreated event for order {} to topic {}", event.orderId(), topic);
        kafkaTemplate.send(topic, event.orderId().toString(), event);
    }
}

package com.orderservice.service;

import com.orderservice.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                               @Value("${app.kafka.topics.order-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @TransactionalEventListener
    // Default phase is AFTER_COMMIT.
    // Other phases: BEFORE_COMMIT, AFTER_ROLLBACK, AFTER_COMPLETION.
    public void send(OrderEvent event) {
        log.debug("Sending {} event for order {} (customer {}) to topic {}",
                event.eventType(), event.orderId(), event.customerId(), topic);
        kafkaTemplate.send(topic, event.customerId().toString(), event);
    }
}

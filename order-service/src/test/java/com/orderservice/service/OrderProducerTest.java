package com.orderservice.service;

import com.orderservice.dto.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class OrderProducerTest {

    private static final String TOPIC = "order-created";

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void send_publishesEventToOrderCreatedTopic() {
        OrderProducer orderProducer = new OrderProducer(kafkaTemplate, TOPIC);
        OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), 15L, "Laptop", 2, Instant.now());

        orderProducer.send(event);

        verify(kafkaTemplate).send(TOPIC, event);
        verifyNoMoreInteractions(kafkaTemplate);
    }
}

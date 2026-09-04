package com.orderservice.service;

import com.orderservice.dto.OrderEvent;
import com.orderservice.dto.OrderEventType;
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
class OrderEventProducerTest {

    private static final String TOPIC = "order-events";

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void send_publishesEventToOrderEventsTopic() {
        OrderEventProducer orderEventProducer = new OrderEventProducer(kafkaTemplate, TOPIC);
        OrderEvent event = OrderEvent.builder()
                .eventType(OrderEventType.CREATED)
                .orderId(UUID.randomUUID())
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .eventTime(Instant.now())
                .build();

        orderEventProducer.send(event);

        verify(kafkaTemplate).send(TOPIC, event.customerId().toString(), event);
        verifyNoMoreInteractions(kafkaTemplate);
    }
}

package com.inventoryservice.integration;

import com.inventoryservice.dto.OrderEvent;
import com.inventoryservice.dto.OrderEventType;
import com.inventoryservice.service.FailureSimulator;
import com.inventoryservice.service.OrderProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = OrderEventListenerIntegrationTest.TOPIC,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class OrderEventListenerIntegrationTest extends TestContainerConfig {

    static final String TOPIC = "order-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoSpyBean
    private OrderProcessingService orderProcessingService;

    @MockitoBean
    private FailureSimulator failureSimulator;

    @Test
    void listener_consumesCreatedEvent() throws Exception {
        OrderEvent event = OrderEvent.builder()
                .eventType(OrderEventType.CREATED)
                .orderId(UUID.randomUUID())
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .eventTime(Instant.now())
                .build();

        kafkaTemplate.send(TOPIC, event).get();

        verify(orderProcessingService, timeout(5000).times(1)).process(event);
    }

    @Test
    void listener_ignoresNonCreatedEvent() throws Exception {
        UUID ignoredOrderId = UUID.randomUUID();
        OrderEvent updatedEvent = OrderEvent.builder()
                .eventType(OrderEventType.UPDATED)
                .orderId(ignoredOrderId)
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .eventTime(Instant.now())
                .build();
        OrderEvent createdEvent = OrderEvent.builder()
                .eventType(OrderEventType.CREATED)
                .orderId(UUID.randomUUID())
                .customerId(16L)
                .product("Phone")
                .quantity(1)
                .eventTime(Instant.now())
                .build();

        // Single partition preserves order, so by the time createdEvent is processed
        // the earlier updatedEvent has already been consumed (and skipped) by the listener.
        kafkaTemplate.send(TOPIC, updatedEvent).get();
        kafkaTemplate.send(TOPIC, createdEvent).get();

        verify(orderProcessingService, timeout(5000).times(1)).process(createdEvent);
        verify(orderProcessingService, never()).process(argThat((OrderEvent e) -> e.orderId().equals(ignoredOrderId)));
    }
}

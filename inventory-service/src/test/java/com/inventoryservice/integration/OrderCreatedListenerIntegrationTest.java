package com.inventoryservice.integration;

import com.inventoryservice.dto.OrderCreatedEvent;
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

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = OrderCreatedListenerIntegrationTest.TOPIC,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class OrderCreatedListenerIntegrationTest extends TestContainerConfig {

    static final String TOPIC = "order-created";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoSpyBean
    private OrderProcessingService orderProcessingService;

    @MockitoBean
    private FailureSimulator failureSimulator;

    @Test
    void listener_consumesOrderCreatedEvent() throws Exception {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(UUID.randomUUID())
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .createdTime(Instant.now())
                .build();

        kafkaTemplate.send(TOPIC, event).get();

        verify(orderProcessingService, timeout(5000).times(1)).process(event);
    }
}

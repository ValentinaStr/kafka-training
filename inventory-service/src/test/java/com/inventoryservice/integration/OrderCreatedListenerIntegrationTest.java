package com.inventoryservice.integration;

import com.inventoryservice.dto.OrderCreatedEvent;
import com.inventoryservice.service.OrderProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = OrderCreatedListenerIntegrationTest.TOPIC)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer"
})
class OrderCreatedListenerIntegrationTest {

    static final String TOPIC = "order-created";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoSpyBean
    private OrderProcessingService orderProcessingService;

    @Test
    void listener_consumesOrderCreatedEvent() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), 15L, "Laptop", 2, Instant.now());

        kafkaTemplate.send(TOPIC, event).get();

        verify(orderProcessingService, timeout(5000).times(1)).process(event);
    }
}

package com.notificationservice.integration;

import com.notificationservice.dto.OrderEvent;
import com.notificationservice.dto.OrderEventType;
import com.notificationservice.service.EmailNotificationService;
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
@EmbeddedKafka(
        partitions = 1,
        topics = OrderEventListenerIntegrationTest.TOPIC,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@TestPropertySource(properties = {
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
        "spring.kafka.producer.properties.spring.json.add.type.headers=false"
})
class OrderEventListenerIntegrationTest {

    static final String TOPIC = "order-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoSpyBean
    private EmailNotificationService emailNotificationService;

    private OrderEvent eventOfType(OrderEventType eventType) {
        return OrderEvent.builder()
                .eventType(eventType)
                .orderId(UUID.randomUUID())
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .eventTime(Instant.now())
                .build();
    }

    @Test
    void listener_consumesCreatedEvent() throws Exception {
        OrderEvent event = eventOfType(OrderEventType.CREATED);

        kafkaTemplate.send(TOPIC, event).get();

        verify(emailNotificationService, timeout(5000).times(1)).send(event);
    }

    @Test
    void listener_consumesUpdatedEvent() throws Exception {
        OrderEvent event = eventOfType(OrderEventType.UPDATED);

        kafkaTemplate.send(TOPIC, event).get();

        verify(emailNotificationService, timeout(5000).times(1)).send(event);
    }

    @Test
    void listener_consumesCancelledEvent() throws Exception {
        OrderEvent event = eventOfType(OrderEventType.CANCELLED);

        kafkaTemplate.send(TOPIC, event).get();

        verify(emailNotificationService, timeout(5000).times(1)).send(event);
    }
}

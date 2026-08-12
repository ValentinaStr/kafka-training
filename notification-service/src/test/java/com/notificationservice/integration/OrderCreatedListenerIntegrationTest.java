package com.notificationservice.integration;

import com.notificationservice.dto.OrderCreatedEvent;
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
        topics = OrderCreatedListenerIntegrationTest.TOPIC,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@TestPropertySource(properties = {
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
        "spring.kafka.producer.properties.spring.json.add.type.headers=false"
})
class OrderCreatedListenerIntegrationTest {

    static final String TOPIC = "order-created";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoSpyBean
    private EmailNotificationService emailNotificationService;

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

        verify(emailNotificationService, timeout(5000).times(1)).sendOrderConfirmation(event);
    }
}

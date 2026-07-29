package com.inventoryservice.integration;

import com.inventoryservice.dto.InventoryResultEvent;
import com.inventoryservice.dto.InventoryStatus;
import com.inventoryservice.dto.OrderCreatedEvent;
import com.inventoryservice.service.FailureSimulator;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {
                OrderCreatedRetryIntegrationTest.ORDER_CREATED_TOPIC,
                OrderCreatedRetryIntegrationTest.INVENTORY_RESULT_TOPIC
        },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class OrderCreatedRetryIntegrationTest {

    static final String ORDER_CREATED_TOPIC = "order-created";
    static final String INVENTORY_RESULT_TOPIC = "inventory-result";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired

    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @MockitoBean
    private FailureSimulator failureSimulator;

    private Consumer<String, InventoryResultEvent> consumer;

    @BeforeEach
    void setUpConsumer() {
        JsonDeserializer<InventoryResultEvent> deserializer = new JsonDeserializer<>(InventoryResultEvent.class);
        deserializer.addTrustedPackages("com.inventoryservice.dto");

        consumer = new DefaultKafkaConsumerFactory<>(
                KafkaTestUtils.consumerProps("verify-retry-inventory-result-group", "true", embeddedKafkaBroker),
                new StringDeserializer(), deserializer)
                .createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, INVENTORY_RESULT_TOPIC);
    }

    @AfterEach
    void tearDownConsumer() {
        consumer.close();
    }

    @Test
    void orderCreated_isRetriedAndEventuallyProcessed_afterTransientFailures() throws Exception {
        doThrow(new RuntimeException("simulated failure 1"))
                .doThrow(new RuntimeException("simulated failure 2"))
                .doNothing()
                .when(failureSimulator).maybeFail();

        UUID orderId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .createdTime(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        kafkaTemplate.send(ORDER_CREATED_TOPIC, orderId.toString(), event).get();

        ConsumerRecord<String, InventoryResultEvent> record =
                KafkaTestUtils.getSingleRecord(consumer, INVENTORY_RESULT_TOPIC, Duration.ofSeconds(15));

        assertThat(record.value().orderId()).isEqualTo(orderId);
        assertThat(record.value().status()).isEqualTo(InventoryStatus.AVAILABLE);
        verify(failureSimulator, timeout(15000).times(3)).maybeFail();
    }
}

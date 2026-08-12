package com.inventoryservice.integration;

import com.inventoryservice.dto.InventoryResultEvent;
import com.inventoryservice.dto.OrderCreatedEvent;
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

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {
                OrderCreatedIdempotencyIntegrationTest.ORDER_CREATED_TOPIC,
                OrderCreatedIdempotencyIntegrationTest.INVENTORY_RESULT_TOPIC
        },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class OrderCreatedIdempotencyIntegrationTest extends TestContainerConfig {

    static final String ORDER_CREATED_TOPIC = "order-created";
    static final String INVENTORY_RESULT_TOPIC = "inventory-result";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, InventoryResultEvent> consumer;

    @BeforeEach
    void setUpConsumer() {
        JsonDeserializer<InventoryResultEvent> deserializer = new JsonDeserializer<>(InventoryResultEvent.class);
        deserializer.addTrustedPackages("com.inventoryservice.dto");

        consumer = new DefaultKafkaConsumerFactory<>(
                KafkaTestUtils.consumerProps("verify-idempotency-group", "true", embeddedKafkaBroker),
                new StringDeserializer(), deserializer)
                .createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, INVENTORY_RESULT_TOPIC);
    }

    @AfterEach
    void tearDownConsumer() {
        consumer.close();
    }

    @Test
    void orderCreated_isProcessedOnlyOnce_whenDeliveredTwice() throws Exception {
        UUID orderId = UUID.fromString("00000000-0000-0000-0000-000000000004");
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .createdTime(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        kafkaTemplate.send(ORDER_CREATED_TOPIC, orderId.toString(), event).get();
        kafkaTemplate.send(ORDER_CREATED_TOPIC, orderId.toString(), event).get();

        ConsumerRecord<String, InventoryResultEvent> record =
                KafkaTestUtils.getSingleRecord(consumer, INVENTORY_RESULT_TOPIC, Duration.ofSeconds(15));

        assertThat(record.value().orderId()).isEqualTo(orderId);
        assertThatThrownBy(() -> KafkaTestUtils.getSingleRecord(consumer, INVENTORY_RESULT_TOPIC, Duration.ofSeconds(3)))
                .isInstanceOf(IllegalStateException.class);
    }
}

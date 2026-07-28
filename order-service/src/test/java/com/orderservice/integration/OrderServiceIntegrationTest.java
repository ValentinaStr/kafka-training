package com.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.dto.OrderCreatedEvent;
import com.orderservice.dto.OrderRequest;
import com.orderservice.entity.OrderEntity;
import com.orderservice.entity.OrderStatus;
import com.orderservice.repository.OrderRepository;
import com.orderservice.service.OrderService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = OrderServiceIntegrationTest.TOPIC,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
) // Starts an embedded Kafka broker inside the test JVM
class OrderServiceIntegrationTest extends TestContainerConfig {

    static final String TOPIC = "order-created";
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    private Consumer<String, String> consumer;

    @BeforeEach
    void setUpConsumer() {
        consumer = new KafkaConsumer<>(
                KafkaTestUtils.consumerProps("verify-group-" + UUID.randomUUID(), "true", embeddedKafkaBroker),
                new StringDeserializer(), new StringDeserializer());
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, TOPIC);
    }

    @AfterEach
    void tearDownConsumer() {
        consumer.close();
    }

    @Test
    void createOrder_shouldSaveOrderAndPublishEventAfterCommit() throws Exception {
        OrderRequest request = OrderRequest.builder().customerId(21L).product("Phone").quantity(1).build();

        UUID orderId = orderService.createOrder(request);

        OrderEntity saved = orderRepository.findById(orderId).orElseThrow();
        assertThat(saved.getCustomerId()).isEqualTo(21L);
        assertThat(saved.getProduct()).isEqualTo("Phone");
        assertThat(saved.getQuantity()).isEqualTo(1);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(saved.getCreatedAt()).isNotNull();

        ConsumerRecord<String, String> record = findRecordForOrder(orderId, Duration.ofSeconds(10));
        OrderCreatedEvent event = objectMapper.readValue(record.value(), OrderCreatedEvent.class);

        assertThat(record.key()).isEqualTo(orderId.toString());
        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.customerId()).isEqualTo(21L);
        assertThat(event.product()).isEqualTo("Phone");
        assertThat(event.quantity()).isEqualTo(1);
        assertThat(event.createdTime()).isNotNull();
    }

    @Test
    @Transactional    // starts a transaction before the test body runs.
    void createOrder_doesNotPublishEventToKafka_whenTransactionRollsBack() {
        OrderRequest request = OrderRequest.builder().customerId(99L).product("Tablet").quantity(4).build();
        UUID orderId = orderService.createOrder(request);

        assertThat(orderRepository.existsById(orderId)).isTrue();

        TestTransaction.flagForRollback();
        TestTransaction.end();

        assertThat(orderRepository.existsById(orderId)).isFalse();
        assertNoRecordForOrder(orderId, Duration.ofSeconds(3));
    }

    private ConsumerRecord<String, String> findRecordForOrder(UUID orderId, Duration timeout) {
        int polls = (int) (timeout.toMillis() / POLL_INTERVAL.toMillis());
        for (int i = 0; i < polls; i++) {
            for (ConsumerRecord<String, String> record : KafkaTestUtils.getRecords(consumer, POLL_INTERVAL).records(TOPIC)) {
                if (orderId.toString().equals(record.key())) {
                    return record;
                }
            }
        }
        throw new IllegalStateException("No record found for order " + orderId + " within " + timeout);
    }

    private void assertNoRecordForOrder(UUID orderId, Duration timeout) {
        int polls = (int) (timeout.toMillis() / POLL_INTERVAL.toMillis());
        for (int i = 0; i < polls; i++) {
            for (ConsumerRecord<String, String> record : KafkaTestUtils.getRecords(consumer, POLL_INTERVAL).records(TOPIC)) {
                assertThat(record.key()).isNotEqualTo(orderId.toString());
            }
        }
    }
}

package com.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.dto.OrderCreatedEvent;
import com.orderservice.service.OrderProducer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = OrderProducerIntegrationTest.TOPIC)
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class OrderProducerIntegrationTest {

    static final String TOPIC = "order-created";

    @Autowired
    private OrderProducer orderProducer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void send_publishesEventAsJsonToOrderCreatedTopic() throws Exception {
        try (Consumer<String, String> consumer = new KafkaConsumer<>(
                KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker),
                new StringDeserializer(), new StringDeserializer())) {

            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, TOPIC);

            OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), 15L, "Laptop", 2, Instant.now());
            orderProducer.send(event);

            ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, TOPIC);
            OrderCreatedEvent received = objectMapper.readValue(record.value(), OrderCreatedEvent.class);

            assertThat(received.orderId()).isEqualTo(event.orderId());
            assertThat(received.customerId()).isEqualTo(event.customerId());
            assertThat(received.product()).isEqualTo(event.product());
            assertThat(received.quantity()).isEqualTo(event.quantity());
            assertThat(received.createdTime()).isEqualTo(event.createdTime());
        }
    }
}

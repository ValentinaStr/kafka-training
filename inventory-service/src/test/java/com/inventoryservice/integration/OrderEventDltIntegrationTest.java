package com.inventoryservice.integration;

import com.inventoryservice.dto.OrderEvent;
import com.inventoryservice.dto.OrderEventType;
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
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

// max-attempts=1 => 0 retries, straight to DLT after the first failure. Keeps the test fast.
@SpringBootTest(properties = "app.kafka.retry.max-attempts=1")
@EmbeddedKafka(
        partitions = 1,
        topics = {
                OrderEventDltIntegrationTest.ORDER_EVENTS_TOPIC,
                OrderEventDltIntegrationTest.DLT_TOPIC
        },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class OrderEventDltIntegrationTest extends TestContainerConfig {

    static final String ORDER_EVENTS_TOPIC = "order-events";
    static final String DLT_TOPIC = "order-events.DLT";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @MockitoBean
    private FailureSimulator failureSimulator;

    private Consumer<String, OrderEvent> dltConsumer;

    @BeforeEach
    void setUpConsumer() {
        JsonDeserializer<OrderEvent> deserializer = new JsonDeserializer<>(OrderEvent.class);
        deserializer.addTrustedPackages("com.inventoryservice.dto");

        dltConsumer = new DefaultKafkaConsumerFactory<>(
                KafkaTestUtils.consumerProps("verify-dlt-group", "true", embeddedKafkaBroker),
                new StringDeserializer(), deserializer)
                .createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(dltConsumer, DLT_TOPIC);
    }

    @AfterEach
    void tearDownConsumer() {
        dltConsumer.close();
    }

    @Test
    void orderCreated_movesToDlt_whenProcessingKeepsFailing() throws Exception {
        doThrow(new RuntimeException("permanent failure")).when(failureSimulator).maybeFail();

        UUID orderId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        OrderEvent event = OrderEvent.builder()
                .eventType(OrderEventType.CREATED)
                .orderId(orderId)
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .eventTime(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        kafkaTemplate.send(ORDER_EVENTS_TOPIC, orderId.toString(), event).get();

        ConsumerRecord<String, OrderEvent> record =
                KafkaTestUtils.getSingleRecord(dltConsumer, DLT_TOPIC, Duration.ofSeconds(15));

        assertThat(record.value().orderId()).isEqualTo(orderId);
        String exceptionMessage = new String(record.headers().lastHeader(KafkaHeaders.DLT_EXCEPTION_MESSAGE).value());
        assertThat(exceptionMessage).contains("permanent failure");
    }
}

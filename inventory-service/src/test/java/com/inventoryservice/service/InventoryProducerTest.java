package com.inventoryservice.service;

import com.inventoryservice.dto.InventoryResultEvent;
import com.inventoryservice.dto.InventoryStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class InventoryProducerTest {

    private static final String TOPIC = "inventory-result";

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void send_publishesEventToInventoryResultTopic() {
        InventoryProducer inventoryProducer = new InventoryProducer(kafkaTemplate, TOPIC);
        InventoryResultEvent event = InventoryResultEvent.builder()
                .orderId(UUID.randomUUID())
                .status(InventoryStatus.AVAILABLE)
                .build();

        inventoryProducer.send(event);

        verify(kafkaTemplate).send(TOPIC, event.orderId().toString(), event);
        verifyNoMoreInteractions(kafkaTemplate);
    }
}

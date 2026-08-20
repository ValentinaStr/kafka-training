package com.inventoryservice.service;

import com.inventoryservice.dto.InventoryResultEvent;
import com.inventoryservice.dto.InventoryStatus;
import com.inventoryservice.dto.OrderEvent;
import com.inventoryservice.entity.ProcessedMessage;
import com.inventoryservice.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final InventoryProducer inventoryProducer;
    private final FailureSimulator failureSimulator;
    private final ProcessedMessageRepository processedMessageRepository;

    @Value("${app.inventory.available-threshold}")
    private int availableThreshold;

    public void process(OrderEvent event) {
        if (processedMessageRepository.existsById(event.orderId())) {
            // counts as a successful delivery to Kafka - the offset is committed as usual
            log.info("Order {} already processed, skipping duplicate CREATED event", event.orderId());
            return;
        }

        System.out.printf(
                "Processing order:%nOrderId: %s%nProduct: %s%nQuantity: %d%n",
                event.orderId(),
                event.product(),
                event.quantity()
        );

        failureSimulator.maybeFail();

        InventoryStatus status = event.quantity() <= availableThreshold ? InventoryStatus.AVAILABLE : InventoryStatus.OUT_OF_STOCK;
        log.info("Order {} inventory status: {}", event.orderId(), status);
        inventoryProducer.send(InventoryResultEvent.builder().orderId(event.orderId()).status(status).build());

        processedMessageRepository.save(ProcessedMessage.builder()
                .orderId(event.orderId())
                .processedAt(Instant.now())
                .build());
        log.info("Order {} marked as processed", event.orderId());
    }
}

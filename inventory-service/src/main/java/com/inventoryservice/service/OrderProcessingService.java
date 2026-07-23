package com.inventoryservice.service;

import com.inventoryservice.dto.InventoryResultEvent;
import com.inventoryservice.dto.InventoryStatus;
import com.inventoryservice.dto.OrderCreatedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class OrderProcessingService {

    private final InventoryProducer inventoryProducer;

    public void process(OrderCreatedEvent event) {
        System.out.printf(
                "Processing order:%nOrderId: %s%nProduct: %s%nQuantity: %d%n",
                event.orderId(),
                event.product(),
                event.quantity()
        );

        InventoryStatus status = event.quantity() <= 5 ? InventoryStatus.AVAILABLE : InventoryStatus.OUT_OF_STOCK;
        log.info("Order {} inventory status: {}", event.orderId(), status);
        inventoryProducer.send(InventoryResultEvent.builder().orderId(event.orderId()).status(status).build());
    }
}

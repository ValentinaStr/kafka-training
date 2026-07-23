package com.inventoryservice.service;

import com.inventoryservice.dto.InventoryResultEvent;
import com.inventoryservice.dto.InventoryStatus;
import com.inventoryservice.dto.OrderCreatedEvent;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderProcessingServiceTest {

    @Mock
    private InventoryProducer inventoryProducer;

    @InjectMocks
    private OrderProcessingService orderProcessingService;

    @ParameterizedTest
    @ValueSource(ints = {1, 5})
    void process_sendsAvailable_whenQuantityIsAtMostFive(int quantity) {
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .customerId(15L)
                .product("Laptop")
                .quantity(quantity)
                .createdTime(Instant.now())
                .build();

        orderProcessingService.process(event);

        verify(inventoryProducer).send(
                InventoryResultEvent.builder().orderId(orderId).status(InventoryStatus.AVAILABLE).build());
    }

    @ParameterizedTest
    @ValueSource(ints = {6, 100})
    void process_sendsOutOfStock_whenQuantityIsAboveFive(int quantity) {
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .customerId(15L)
                .product("Laptop")
                .quantity(quantity)
                .createdTime(Instant.now())
                .build();

        orderProcessingService.process(event);

        verify(inventoryProducer).send(
                InventoryResultEvent.builder().orderId(orderId).status(InventoryStatus.OUT_OF_STOCK).build());
    }
}

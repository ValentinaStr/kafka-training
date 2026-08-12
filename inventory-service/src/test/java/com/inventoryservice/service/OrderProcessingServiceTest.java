package com.inventoryservice.service;

import com.inventoryservice.dto.InventoryResultEvent;
import com.inventoryservice.dto.InventoryStatus;
import com.inventoryservice.dto.OrderCreatedEvent;
import com.inventoryservice.repository.ProcessedMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderProcessingServiceTest {

    @Mock
    private InventoryProducer inventoryProducer;

    @Mock
    private FailureSimulator failureSimulator;

    @Mock
    private ProcessedMessageRepository processedMessageRepository;

    @InjectMocks
    private OrderProcessingService orderProcessingService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderProcessingService, "availableThreshold", 5);
    }

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
        verify(processedMessageRepository).save(argThat(m -> m.getOrderId().equals(orderId)));
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

    @Test
    void process_propagatesExceptionAndDoesNotPublish_whenFailureSimulatorThrows() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(UUID.randomUUID())
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .createdTime(Instant.now())
                .build();
        doThrow(new RuntimeException("simulated failure")).when(failureSimulator).maybeFail();

        assertThatThrownBy(() -> orderProcessingService.process(event))
                .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(inventoryProducer);
        verify(processedMessageRepository, never()).save(any());
    }

    @Test
    void process_skipsDuplicate_whenAlreadyProcessed() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(UUID.randomUUID())
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .createdTime(Instant.now())
                .build();
        when(processedMessageRepository.existsById(event.orderId())).thenReturn(true);

        orderProcessingService.process(event);

        verifyNoInteractions(failureSimulator, inventoryProducer);
        verify(processedMessageRepository, never()).save(any());
    }
}

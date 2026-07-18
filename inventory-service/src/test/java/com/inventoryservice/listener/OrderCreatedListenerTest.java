package com.inventoryservice.listener;

import com.inventoryservice.dto.OrderCreatedEvent;
import com.inventoryservice.service.OrderProcessingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCreatedListenerTest {

    @Mock
    private OrderProcessingService orderProcessingService;

    @Test
    void onOrderCreated_callsProcessingService() {
        OrderCreatedListener listener = new OrderCreatedListener(orderProcessingService);
        OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), 15L, "Laptop", 2, Instant.now());

        listener.onOrderCreated(event);

        verify(orderProcessingService).process(event);
    }
}

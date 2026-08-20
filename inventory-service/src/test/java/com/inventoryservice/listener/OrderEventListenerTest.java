package com.inventoryservice.listener;

import com.inventoryservice.dto.OrderEvent;
import com.inventoryservice.dto.OrderEventType;
import com.inventoryservice.service.OrderProcessingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @Mock
    private OrderProcessingService orderProcessingService;

    @InjectMocks
    private OrderEventListener listener;

    @Test
    void onOrderEvent_callsProcessingService_whenEventTypeIsCreated() {
        OrderEvent event = OrderEvent.builder()
                .eventType(OrderEventType.CREATED)
                .orderId(UUID.randomUUID())
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .eventTime(Instant.now())
                .build();

        listener.onOrderEvent(event);

        verify(orderProcessingService).process(event);
    }

    @Test
    void onOrderEvent_ignoresEvent_whenEventTypeIsNotCreated() {
        OrderEvent event = OrderEvent.builder()
                .eventType(OrderEventType.UPDATED)
                .orderId(UUID.randomUUID())
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .eventTime(Instant.now())
                .build();

        listener.onOrderEvent(event);

        verifyNoInteractions(orderProcessingService);
    }
}

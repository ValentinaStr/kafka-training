package com.inventoryservice.listener;

import com.inventoryservice.dto.OrderCreatedEvent;
import com.inventoryservice.service.DltMonitorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DltMonitorListenerTest {

    @Mock
    private DltMonitorService dltMonitorService;

    @InjectMocks
    private DltMonitorListener listener;

    @Test
    void handleDeadLetter_callsDltMonitorService() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(UUID.randomUUID())
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .createdTime(Instant.now())
                .build();

        listener.handleDeadLetter(event, "simulated failure");

        verify(dltMonitorService).report(event, "simulated failure");
    }
}

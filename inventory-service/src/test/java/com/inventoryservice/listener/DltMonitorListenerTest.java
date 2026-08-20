package com.inventoryservice.listener;

import com.inventoryservice.dto.OrderEvent;
import com.inventoryservice.dto.OrderEventType;
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
        OrderEvent event = OrderEvent.builder()
                .eventType(OrderEventType.CREATED)
                .orderId(UUID.randomUUID())
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .eventTime(Instant.now())
                .build();

        listener.handleDeadLetter(event, "simulated failure");

        verify(dltMonitorService).report(event, "simulated failure");
    }
}

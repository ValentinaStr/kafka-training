package com.notificationservice.listener;

import com.notificationservice.dto.OrderEvent;
import com.notificationservice.dto.OrderEventType;
import com.notificationservice.service.EmailNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private OrderEventListener listener;

    @Test
    void onOrderEvent_delegatesToEmailNotificationService() {
        OrderEvent event = OrderEvent.builder()
                .eventType(OrderEventType.CREATED)
                .orderId(UUID.randomUUID())
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .eventTime(Instant.now())
                .build();

        listener.onOrderEvent(event);

        verify(emailNotificationService).send(event);
    }
}

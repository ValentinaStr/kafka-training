package com.notificationservice.listener;

import com.notificationservice.dto.OrderCreatedEvent;
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
class OrderCreatedListenerTest {

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private OrderCreatedListener listener;

    @Test
    void onOrderCreated_callsEmailNotificationService() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(UUID.randomUUID())
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .createdTime(Instant.now())
                .build();

        listener.onOrderCreated(event);

        verify(emailNotificationService).sendOrderConfirmation(event);
    }
}

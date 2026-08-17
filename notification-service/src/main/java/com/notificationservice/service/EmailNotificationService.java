package com.notificationservice.service;

import com.notificationservice.dto.OrderCreatedEvent;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    public void sendOrderConfirmation(OrderCreatedEvent event) {
        System.out.printf(
                "Email sent to customer %d: your order %s for %d x %s has been received.%n",
                event.customerId(),
                event.orderId(),
                event.quantity(),
                event.product()
        );
    }
}

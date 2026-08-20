package com.notificationservice.service;

import com.notificationservice.dto.OrderEvent;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    public void send(OrderEvent event) {
        String message = switch (event.eventType()) {
            case CREATED -> "has been received.";
            case UPDATED -> "has been updated.";
            case CANCELLED -> "has been cancelled.";
        };

        sendEmail(event, message);
    }

    private void sendEmail(OrderEvent event, String message) {
        System.out.printf(
                "Email sent to customer %d: your order %s for %d x %s %s%n",
                event.customerId(),
                event.orderId(),
                event.quantity(),
                event.product(),
                message
        );
    }
}

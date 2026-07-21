package com.inventoryservice.service;

import com.inventoryservice.dto.OrderCreatedEvent;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessingService {

    public void process(OrderCreatedEvent event) {
        System.out.printf(
                "Processing order:%nOrderId: %s%nProduct: %s%nQuantity: %d%n",
                event.orderId(),
                event.product(),
                event.quantity()
        );
    }
}

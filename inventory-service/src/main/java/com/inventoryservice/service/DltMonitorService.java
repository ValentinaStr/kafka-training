package com.inventoryservice.service;

import com.inventoryservice.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DltMonitorService {

    public void report(OrderCreatedEvent event, String exceptionMessage) {
        System.out.printf(
                "DLT message:%nOrderId: %s%nProduct: %s%nQuantity: %d%nReason: %s%n",
                event.orderId(),
                event.product(),
                event.quantity(),
                exceptionMessage
        );
        log.error("DLT message received: orderId={}, product={}, quantity={}, reason={}",
                event.orderId(), event.product(), event.quantity(), exceptionMessage);
    }
}

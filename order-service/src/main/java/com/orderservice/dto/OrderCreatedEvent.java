package com.orderservice.dto;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        Long customerId,
        String product,
        Integer quantity,
        Instant createdTime
) {
}

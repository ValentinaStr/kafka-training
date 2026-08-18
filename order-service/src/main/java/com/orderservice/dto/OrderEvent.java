package com.orderservice.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderEvent(
        OrderEventType eventType,
        UUID orderId,
        Long customerId,
        String product,
        Integer quantity,
        Instant eventTime
) {
}

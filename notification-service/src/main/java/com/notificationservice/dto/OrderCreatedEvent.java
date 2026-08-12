package com.notificationservice.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderCreatedEvent(
        UUID orderId,
        Long customerId,
        String product,
        Integer quantity,
        Instant createdTime
) {
}

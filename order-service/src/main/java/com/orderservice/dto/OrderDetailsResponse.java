package com.orderservice.dto;

import com.orderservice.entity.OrderStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderDetailsResponse(
        UUID orderId,
        Long customerId,
        String product,
        Integer quantity,
        OrderStatus status,
        Instant createdAt) {
}

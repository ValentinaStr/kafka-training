package com.orderservice.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderResponse(UUID orderId) {
}

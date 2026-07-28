package com.orderservice.dto;

import com.orderservice.entity.OrderStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record InventoryResult(UUID orderId, OrderStatus status) {
}

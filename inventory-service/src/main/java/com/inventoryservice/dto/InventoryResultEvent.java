package com.inventoryservice.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record InventoryResultEvent(UUID orderId, InventoryStatus status) {
}

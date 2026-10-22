package com.orderservice.dto;

import com.orderservice.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Message consumed from the inventory-result Kafka topic, published by inventory-service "
        + "after it checks stock for an order.")
public record InventoryResult(
        @Schema(description = "Id of the order the stock check was performed for", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID orderId,
        @Schema(description = "Outcome of the stock check")
        OrderStatus status) {
}

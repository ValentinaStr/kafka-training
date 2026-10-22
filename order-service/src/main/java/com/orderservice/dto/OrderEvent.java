package com.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * There are three common ways to route multiple event types from one Kafka topic:
 *
 * 1. Spring Kafka @KafkaHandler:
 *    Each event type is mapped to a Java class using Kafka type headers.
 *    Simple, but routing depends on Java/Spring-specific metadata and is not
 *    language-independent.
 *
 * 2. Flat envelope (used here):
 *    One OrderEvent DTO contains all event types and an eventType discriminator.
 *    The consumer switches on eventType. This keeps the message self-describing
 *    and fits the project design where each service has its own DTOs.
 *    The trade-off is one shared schema containing fields that some event types
 *    may not use.
 *
 * 3. Wrapping envelope:
 *    A generic {type, payload} object keeps the event-specific payload separate.
 *    It is also language-independent and allows each event type to have its own
 *    schema, but requires an extra deserialization step.
 */
@Builder
@Schema(description = "Message published to the order-events Kafka topic. Carries CREATED, UPDATED, "
        + "and CANCELLED events in one flat envelope, discriminated by eventType.")
public record OrderEvent(
        @Schema(description = "Discriminator identifying which lifecycle event this message represents")
        OrderEventType eventType,
        @Schema(description = "Id of the order this event refers to", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID orderId,
        @Schema(description = "Id of the customer who placed the order, used as the Kafka message key", example = "15")
        Long customerId,
        @Schema(description = "Product name", example = "Laptop")
        String product,
        @Schema(description = "Quantity ordered", example = "2")
        Integer quantity,
        @Schema(description = "Time this event was published", example = "2026-10-22T10:00:00Z")
        Instant eventTime
) {
}

package com.orderservice.dto;

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
public record OrderEvent(
        OrderEventType eventType,
        UUID orderId,
        Long customerId,
        String product,
        Integer quantity,
        Instant eventTime
) {
}

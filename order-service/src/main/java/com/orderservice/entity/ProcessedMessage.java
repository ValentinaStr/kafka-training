package com.orderservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "processed_messages")
// No cleanup yet - Kafka's default topic retention is 7 days, so redelivery past that point is impossible; a periodic purge of older rows should be added.
public class ProcessedMessage {

    @Id
    private UUID orderId;

    @Column(nullable = false)
    private Instant processedAt;
}

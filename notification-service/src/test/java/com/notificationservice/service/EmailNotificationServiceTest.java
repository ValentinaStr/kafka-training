package com.notificationservice.service;

import com.notificationservice.dto.OrderEvent;
import com.notificationservice.dto.OrderEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNotificationServiceTest {

    private final EmailNotificationService service = new EmailNotificationService();
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void redirectStdOut() {
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    void restoreStdOut() {
        System.setOut(originalOut);
    }

    private OrderEvent eventOfType(OrderEventType eventType) {
        return OrderEvent.builder()
                .eventType(eventType)
                .orderId(UUID.randomUUID())
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .eventTime(Instant.now())
                .build();
    }

    @Test
    void send_printsConfirmation_whenEventTypeIsCreated() {
        service.send(eventOfType(OrderEventType.CREATED));

        assertThat(output.toString()).contains("has been received");
    }

    @Test
    void send_printsUpdate_whenEventTypeIsUpdated() {
        service.send(eventOfType(OrderEventType.UPDATED));

        assertThat(output.toString()).contains("has been updated");
    }

    @Test
    void send_printsCancellation_whenEventTypeIsCancelled() {
        service.send(eventOfType(OrderEventType.CANCELLED));

        assertThat(output.toString()).contains("has been cancelled");
    }
}

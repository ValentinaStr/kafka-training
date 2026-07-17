package com.orderservice.controller;

import com.orderservice.dto.OrderCreatedEvent;
import com.orderservice.dto.OrderRequest;
import com.orderservice.service.OrderProducer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderProducer producer;

    public OrderController(OrderProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public OrderCreatedEvent createOrder(@RequestBody OrderRequest request) {

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                request.customerId(),
                request.product(),
                request.quantity(),
                Instant.now()
        );
       producer.send(event);

        return event;
    }
}

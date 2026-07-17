package com.orderservice.controller;

import com.orderservice.dto.OrderCreatedEvent;
import com.orderservice.dto.OrderRequest;
import com.orderservice.dto.OrderResponse;
import com.orderservice.service.OrderProducer;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(), request.customerId(), request.product(), request.quantity(), Instant.now());

        producer.send(event);

        return new OrderResponse(event.orderId());
    }
}

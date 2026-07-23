package com.orderservice.controller;

import com.orderservice.dto.OrderRequest;
import com.orderservice.dto.OrderResponse;
import com.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Received order request for customer {}: product={}, quantity={}",
                request.customerId(), request.product(), request.quantity());
        UUID orderId = orderService.createOrder(request);
        log.info("Order {} accepted", orderId);
        return OrderResponse.builder().orderId(orderId).build();
    }
}

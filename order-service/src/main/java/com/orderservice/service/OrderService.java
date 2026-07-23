package com.orderservice.service;

import com.orderservice.dto.OrderCreatedEvent;
import com.orderservice.dto.OrderRequest;
import com.orderservice.entity.OrderEntity;
import com.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UUID createOrder(OrderRequest request) {
        OrderEntity savedOrder = orderRepository.save(mapOrderRequestToOrderEntity(request));
        log.info("Order {} saved successfully", savedOrder.getId());

        eventPublisher.publishEvent(mapOrderEntityToOrderCreatedEvent(savedOrder));

        return savedOrder.getId();
    }

    private OrderEntity mapOrderRequestToOrderEntity(OrderRequest request) {
        return OrderEntity.builder()
                .customerId(request.customerId())
                .product(request.product())
                .quantity(request.quantity())
                .createdAt(Instant.now())
                .build();
    }

    private OrderCreatedEvent mapOrderEntityToOrderCreatedEvent(OrderEntity savedOrder) {
        return OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .customerId(savedOrder.getCustomerId())
                .product(savedOrder.getProduct())
                .quantity(savedOrder.getQuantity())
                .createdTime(savedOrder.getCreatedAt())
                .build();
    }
}

package com.orderservice.service;

import com.orderservice.dto.InventoryResult;
import com.orderservice.dto.OrderCreatedEvent;
import com.orderservice.dto.OrderRequest;
import com.orderservice.entity.OrderEntity;
import com.orderservice.entity.ProcessedMessage;
import com.orderservice.repository.OrderRepository;
import com.orderservice.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProcessedMessageRepository processedMessageRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UUID createOrder(OrderRequest request) {
        OrderEntity savedOrder = orderRepository.save(mapOrderRequestToOrderEntity(request));
        log.info("Order {} saved successfully", savedOrder.getId());

        eventPublisher.publishEvent(mapOrderEntityToOrderCreatedEvent(savedOrder));

        return savedOrder.getId();
    }

    @Transactional
    public void updateOrderStatus(InventoryResult inventoryResult) {
        UUID orderId = inventoryResult.orderId();
        if (processedMessageRepository.existsById(orderId)) {
            log.info("Order {} already processed, skipping duplicate inventory result", orderId);
            return;
        }

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
        order.setStatus(inventoryResult.status());

        processedMessageRepository.save(ProcessedMessage.builder()
                .orderId(orderId)
                .processedAt(Instant.now())
                .build());
        log.info("Order {} status updated to {}", order.getId(), order.getStatus());
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

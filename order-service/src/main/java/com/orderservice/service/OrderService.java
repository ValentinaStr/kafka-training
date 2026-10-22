package com.orderservice.service;

import com.orderservice.dto.InventoryResult;
import com.orderservice.dto.OrderDetailsResponse;
import com.orderservice.dto.OrderEvent;
import com.orderservice.dto.OrderEventType;
import com.orderservice.dto.OrderRequest;
import com.orderservice.entity.OrderEntity;
import com.orderservice.entity.OrderStatus;
import com.orderservice.entity.ProcessedMessage;
import com.orderservice.exception.OrderNotFoundException;
import com.orderservice.repository.OrderRepository;
import com.orderservice.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
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

        eventPublisher.publishEvent(mapOrderEntityToOrderEvent(savedOrder, OrderEventType.CREATED));

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

        OrderEventType eventType = order.getStatus() == OrderStatus.OUT_OF_STOCK
                ? OrderEventType.CANCELLED
                : OrderEventType.UPDATED;
        eventPublisher.publishEvent(mapOrderEntityToOrderEvent(order, eventType));
    }

    public OrderDetailsResponse getOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return mapOrderEntityToOrderDetailsResponse(order);
    }

    public List<OrderDetailsResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapOrderEntityToOrderDetailsResponse)
                .toList();
    }

    private OrderDetailsResponse mapOrderEntityToOrderDetailsResponse(OrderEntity order) {
        return OrderDetailsResponse.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .product(order.getProduct())
                .quantity(order.getQuantity())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderEntity mapOrderRequestToOrderEntity(OrderRequest request) {
        return OrderEntity.builder()
                .customerId(request.customerId())
                .product(request.product())
                .quantity(request.quantity())
                .createdAt(Instant.now())
                .build();
    }

    private OrderEvent mapOrderEntityToOrderEvent(OrderEntity order, OrderEventType eventType) {
        return OrderEvent.builder()
                .eventType(eventType)
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .product(order.getProduct())
                .quantity(order.getQuantity())
                .eventTime(Instant.now())
                .build();
    }
}

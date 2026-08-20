package com.orderservice.service;

import com.orderservice.dto.InventoryResult;
import com.orderservice.dto.OrderEvent;
import com.orderservice.dto.OrderEventType;
import com.orderservice.dto.OrderRequest;
import com.orderservice.entity.OrderEntity;
import com.orderservice.entity.OrderStatus;
import com.orderservice.repository.OrderRepository;
import com.orderservice.repository.ProcessedMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProcessedMessageRepository processedMessageRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private final OrderEntity orderEntity =
            OrderEntity.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                    .customerId(101L)
                    .product("car")
                    .quantity(3)
                    .createdAt(Instant.parse("2020-01-01T00:00:00Z"))
                    .build();

    @Test
    void createOrder_savesOrderAndPublishesCreatedEvent_whenSaveSucceeds() {
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
        OrderRequest request = OrderRequest.builder().customerId(101L).product("car").quantity(3).build();

        var result = orderService.createOrder(request);

        assertThat(result).isEqualTo(orderEntity.getId());
        verify(orderRepository).save(any(OrderEntity.class));

        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(OrderEventType.CREATED);
        assertThat(captor.getValue().orderId()).isEqualTo(orderEntity.getId());
    }

    @Test
    void createOrder_doesNotPublishEvent_whenSaveFails() {
        when(orderRepository.save(any(OrderEntity.class)))
                .thenThrow(new RuntimeException("DB error"));
        OrderRequest request = OrderRequest.builder().customerId(101L).product("car").quantity(3).build();

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(RuntimeException.class);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void updateOrderStatus_throwsNoSuchElementException_whenOrderNotFound() {
        UUID orderId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        InventoryResult inventoryResult = InventoryResult.builder().orderId(orderId).status(OrderStatus.AVAILABLE).build();

        assertThatThrownBy(() -> orderService.updateOrderStatus(inventoryResult))
                .isInstanceOf(NoSuchElementException.class);
        verify(processedMessageRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_skipsDuplicate_whenAlreadyProcessed() {
        UUID orderId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        InventoryResult inventoryResult = InventoryResult.builder().orderId(orderId).status(OrderStatus.AVAILABLE).build();
        when(processedMessageRepository.existsById(orderId)).thenReturn(true);

        orderService.updateOrderStatus(inventoryResult);

        verify(orderRepository, never()).findById(any());
        verify(processedMessageRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_publishesUpdatedEvent_whenStatusIsAvailable() {
        InventoryResult inventoryResult = InventoryResult.builder().orderId(orderEntity.getId()).status(OrderStatus.AVAILABLE).build();
        when(orderRepository.findById(orderEntity.getId())).thenReturn(Optional.of(orderEntity));

        orderService.updateOrderStatus(inventoryResult);

        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(OrderEventType.UPDATED);
    }

    @Test
    void updateOrderStatus_publishesCancelledEvent_whenStatusIsOutOfStock() {
        InventoryResult inventoryResult = InventoryResult.builder().orderId(orderEntity.getId()).status(OrderStatus.OUT_OF_STOCK).build();
        when(orderRepository.findById(orderEntity.getId())).thenReturn(Optional.of(orderEntity));

        orderService.updateOrderStatus(inventoryResult);

        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(OrderEventType.CANCELLED);
    }
}

package com.orderservice.service;

import com.orderservice.dto.OrderCreatedEvent;
import com.orderservice.dto.OrderRequest;
import com.orderservice.entity.OrderEntity;
import com.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

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
    void createOrder_savesOrderAndPublishesEvent_whenSaveSucceeds() {
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
        OrderRequest request = OrderRequest.builder().customerId(101L).product("car").quantity(3).build();

        var result = orderService.createOrder(request);

        assertThat(result).isEqualTo(orderEntity.getId());
        verify(orderRepository).save(any(OrderEntity.class));
        verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
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
}

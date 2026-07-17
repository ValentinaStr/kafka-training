package com.orderservice.controller;

import com.orderservice.dto.OrderCreatedEvent;
import com.orderservice.dto.OrderRequest;
import com.orderservice.dto.OrderResponse;
import com.orderservice.service.OrderProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderProducer orderProducer;

    @Captor
    private ArgumentCaptor<OrderCreatedEvent> eventCaptor;

    @Test
    void createOrder_generatesEventAndPublishesIt() {
        OrderController controller = new OrderController(orderProducer);
        OrderRequest request = new OrderRequest(15L, "Laptop", 2);

        OrderResponse response = controller.createOrder(request);

        verify(orderProducer).send(eventCaptor.capture());
        OrderCreatedEvent event = eventCaptor.getValue();

        assertThat(event.orderId()).isNotNull();
        assertThat(event.customerId()).isEqualTo(15L);
        assertThat(event.product()).isEqualTo("Laptop");
        assertThat(event.quantity()).isEqualTo(2);
        assertThat(event.createdTime()).isNotNull();

        assertThat(response.orderId()).isEqualTo(event.orderId());
    }
}

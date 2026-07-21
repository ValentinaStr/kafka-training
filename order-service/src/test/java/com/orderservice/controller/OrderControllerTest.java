package com.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.dto.OrderCreatedEvent;
import com.orderservice.dto.OrderRequest;
import com.orderservice.service.OrderProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderProducer orderProducer;

    @Test
    void createOrder_shouldReturnAcceptedAndPublishEvent_Positive() throws Exception {
        OrderRequest request = new OrderRequest(15L, "Laptop", 2);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").exists());

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(orderProducer).send(captor.capture());

        OrderCreatedEvent event = captor.getValue();
        assertThat(event.orderId()).isNotNull();
        assertThat(event.customerId()).isEqualTo(15L);
        assertThat(event.product()).isEqualTo("Laptop");
        assertThat(event.quantity()).isEqualTo(2);
        assertThat(event.createdTime()).isNotNull();
    }

    @Test
    void createOrder_shouldReturnBadRequest_whenProductNull_Negative() throws Exception {
        OrderRequest request = new OrderRequest(15L, null, 2);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderProducer);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void createOrder_shouldReturnBadRequest_whenQuantityIsNotPositive_Negative(int quantity) throws Exception {
        OrderRequest request = new OrderRequest(15L, "Laptop", quantity);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderProducer);
    }
}

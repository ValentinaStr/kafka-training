package com.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.dto.OrderDetailsResponse;
import com.orderservice.dto.OrderRequest;
import com.orderservice.entity.OrderStatus;
import com.orderservice.exception.OrderNotFoundException;
import com.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrder_returnsAcceptedWithOrderId_whenRequestIsValid() throws Exception {
        UUID orderId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        OrderRequest request = OrderRequest.builder().customerId(15L).product("Laptop").quantity(2).build();
        when(orderService.createOrder(request)).thenReturn(orderId);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));
    }

    @Test
    void createOrder_returnsBadRequest_whenProductIsNull() throws Exception {
        OrderRequest request = OrderRequest.builder().customerId(15L).product(null).quantity(2).build();

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("product: must not be null"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(orderService);
    }

    @Test
    void createOrder_returnsBadRequest_whenBodyIsMalformedJson() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"));

        verifyNoInteractions(orderService);
    }

    @Test
    void createOrder_returnsInternalServerError_whenServiceThrowsUnexpectedException() throws Exception {
        OrderRequest request = OrderRequest.builder().customerId(15L).product("Laptop").quantity(2).build();
        doThrow(new RuntimeException("boom")).when(orderService).createOrder(request);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void createOrder_returnsBadRequest_whenQuantityIsNotPositive(int quantity) throws Exception {
        OrderRequest request = OrderRequest.builder().customerId(15L).product("Laptop").quantity(quantity).build();

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verifyNoInteractions(orderService);
    }

    @Test
    void getOrder_returnsOrderDetails_whenOrderExists() throws Exception {
        UUID orderId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        OrderDetailsResponse response = OrderDetailsResponse.builder()
                .orderId(orderId)
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .status(OrderStatus.AVAILABLE)
                .createdAt(Instant.parse("2026-10-22T10:00:00Z"))
                .build();
        when(orderService.getOrder(orderId)).thenReturn(response);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.customerId").value(15))
                .andExpect(jsonPath("$.product").value("Laptop"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void getOrder_returnsNotFound_whenOrderDoesNotExist() throws Exception {
        UUID orderId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        when(orderService.getOrder(orderId)).thenThrow(new OrderNotFoundException(orderId));

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Order with id " + orderId + " was not found"));
    }

    @Test
    void getAllOrders_returnsListOfOrders() throws Exception {
        OrderDetailsResponse response = OrderDetailsResponse.builder()
                .orderId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .customerId(15L)
                .product("Laptop")
                .quantity(2)
                .status(OrderStatus.NEW)
                .createdAt(Instant.parse("2026-10-22T10:00:00Z"))
                .build();
        when(orderService.getAllOrders()).thenReturn(List.of(response));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(response.orderId().toString()))
                .andExpect(jsonPath("$[0].product").value("Laptop"));
    }
}

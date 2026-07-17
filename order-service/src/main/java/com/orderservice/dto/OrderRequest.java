package com.orderservice.dto;

public record OrderRequest(
        Long customerId,
        String product,
        Integer quantity) {
}

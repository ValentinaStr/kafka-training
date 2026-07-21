package com.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderRequest(
        Long customerId,
        @NotNull
        String product,
        @Positive
        Integer quantity) {
}

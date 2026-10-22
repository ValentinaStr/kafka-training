package com.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle stage of an order event on order-events")
public enum OrderEventType {CREATED, UPDATED, CANCELLED}

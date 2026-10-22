package com.orderservice.controller;

import com.orderservice.dto.OrderDetailsResponse;
import com.orderservice.dto.OrderRequest;
import com.orderservice.dto.OrderResponse;
import com.orderservice.exception.ErrorResponse;
import com.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Create orders and track their status")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create an order", description = "Saves the order as NEW and publishes a CREATED event to Kafka for inventory processing.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Order accepted and queued for inventory processing",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrderResponse.class),
                            examples = @ExampleObject(value = "{\"orderId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"}"))),
            @ApiResponse(responseCode = "400", description = "Request failed validation (e.g. missing product, non-positive quantity) or body is unreadable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "VALIDATION_ERROR", value = "{\"timestamp\": \"2026-10-22T10:00:00Z\", \"status\": 400, \"error\": \"VALIDATION_ERROR\", \"message\": \"product: must not be null\"}"),
                                    @ExampleObject(name = "MALFORMED_REQUEST", value = "{\"timestamp\": \"2026-10-22T10:00:00Z\", \"status\": 400, \"error\": \"MALFORMED_REQUEST\", \"message\": \"Request body is missing or unreadable\"}")
                            })),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"timestamp\": \"2026-10-22T10:00:00Z\", \"status\": 500, \"error\": \"INTERNAL_ERROR\", \"message\": \"An unexpected error occurred\"}")))
    })
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody(description = "Order to create", required = true,
                    content = @Content(examples = @ExampleObject(value = "{\"customerId\": 15, \"product\": \"Laptop\", \"quantity\": 2}")))
            @Valid @org.springframework.web.bind.annotation.RequestBody OrderRequest request) {
        log.info("Received order request for customer {}: product={}, quantity={}",
                request.customerId(), request.product(), request.quantity());
        UUID orderId = orderService.createOrder(request);
        log.info("Order {} accepted", orderId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(OrderResponse.builder().orderId(orderId).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id", description = "Returns the current state of a single order, including its status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrderDetailsResponse.class),
                            examples = @ExampleObject(value = "{\"orderId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"customerId\": 15, \"product\": \"Laptop\", \"quantity\": 2, \"status\": \"AVAILABLE\", \"createdAt\": \"2026-10-22T10:00:00Z\"}"))),
            @ApiResponse(responseCode = "404", description = "No order exists with the given id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"timestamp\": \"2026-10-22T10:00:00Z\", \"status\": 404, \"error\": \"ORDER_NOT_FOUND\", \"message\": \"Order with id 3fa85f64-5717-4562-b3fc-2c963f66afa6 was not found\"}"))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"timestamp\": \"2026-10-22T10:00:00Z\", \"status\": 500, \"error\": \"INTERNAL_ERROR\", \"message\": \"An unexpected error occurred\"}")))
    })
    public ResponseEntity<OrderDetailsResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping
    @Operation(summary = "List all orders", description = "Returns every order currently stored.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of orders",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = OrderDetailsResponse.class)))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"timestamp\": \"2026-10-22T10:00:00Z\", \"status\": 500, \"error\": \"INTERNAL_ERROR\", \"message\": \"An unexpected error occurred\"}")))
    })
    public ResponseEntity<List<OrderDetailsResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}

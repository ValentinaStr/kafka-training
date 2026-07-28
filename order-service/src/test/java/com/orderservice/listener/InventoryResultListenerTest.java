package com.orderservice.listener;

import com.orderservice.dto.InventoryResult;
import com.orderservice.entity.OrderStatus;
import com.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryResultListenerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private InventoryResultListener listener;

    @Test
    void onInventoryResult_callsOrderService() {
        InventoryResult result = InventoryResult.builder().orderId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .status(OrderStatus.AVAILABLE).build();
        listener.onInventoryResult(result);
        verify(orderService).updateOrderStatus(result);
    }
}

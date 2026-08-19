package com.inventoryservice.listener;

import com.inventoryservice.dto.OrderEvent;
import com.inventoryservice.dto.OrderEventType;
import com.inventoryservice.service.OrderProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderProcessingService orderProcessingService;

    // Non-blocking retry via separate retry/DLT topics.
    // @RetryableTopic(attempts = "${app.kafka.retry.max-attempts}",
    //         backoff = @Backoff(delayExpression = "${app.kafka.retry.backoff-ms}"))
    @KafkaListener(topics = "${app.kafka.topics.order-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderEvent(OrderEvent event) {
        if (event.eventType() != OrderEventType.CREATED) {
            log.debug("Ignoring {} event for order {}", event.eventType(), event.orderId());
            return;
        }
        orderProcessingService.process(event);
    }
}

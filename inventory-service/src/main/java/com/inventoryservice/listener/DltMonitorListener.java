package com.inventoryservice.listener;

import com.inventoryservice.dto.OrderCreatedEvent;
import com.inventoryservice.service.DltMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DltMonitorListener {

    private final DltMonitorService dltMonitorService;

    @KafkaListener(topics = "${app.kafka.topics.order-created-dlt}", groupId = "dlt-monitor")
    public void handleDeadLetter(
            OrderCreatedEvent event,
            @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage) {
        dltMonitorService.report(event, exceptionMessage);
    }
}

// Headers DeadLetterPublishingRecoverer adds to every DLT record:
//
// DLT_ORIGINAL_TOPIC            - the topic the message originally came from ("order-created")
// DLT_ORIGINAL_PARTITION        - its partition in that original topic
// DLT_ORIGINAL_OFFSET           - its offset in that original topic
// DLT_ORIGINAL_TIMESTAMP        - when the message was first written to the original topic
// DLT_ORIGINAL_TIMESTAMP_TYPE   - CreateTime or LogAppendTime
// DLT_EXCEPTION_FQCN            - full class name of the exception (e.g. java.lang.RuntimeException)
// DLT_EXCEPTION_MESSAGE         - the exception's message text (getMessage()) - the one read above
// DLT_EXCEPTION_STACKTRACE      - the full stack trace as a string
// DLT_EXCEPTION_CAUSE_FQCN      - class name of the exception's cause, if any
// DLT_KEY_EXCEPTION_FQCN/MESSAGE/STACKTRACE - same as above, but for failures deserializing the KEY, not the value

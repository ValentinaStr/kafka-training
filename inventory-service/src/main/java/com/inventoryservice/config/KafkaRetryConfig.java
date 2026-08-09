package com.inventoryservice.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaRetryConfig {

    @Bean
    public DeadLetterPublishingRecoverer deadLetterRecoverer(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${app.kafka.topics.order-created-dlt}") String dltTopic) {

        return new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> new TopicPartition(dltTopic, record.partition())
        );
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            @Value("${app.kafka.retry.backoff-ms}") long backoffMs,
            @Value("${app.kafka.retry.max-attempts}") long maxAttempts,
            DeadLetterPublishingRecoverer deadLetterRecoverer) {
        return new DefaultErrorHandler(deadLetterRecoverer, new FixedBackOff(backoffMs, maxAttempts - 1));
    }
}

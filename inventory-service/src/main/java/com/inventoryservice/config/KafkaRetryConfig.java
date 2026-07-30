package com.inventoryservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaRetryConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            @Value("${app.kafka.retry.backoff-ms}") long backoffMs,
            @Value("${app.kafka.retry.max-attempts}") long maxAttempts) {
        return new DefaultErrorHandler(new FixedBackOff(backoffMs, maxAttempts - 1));
    }
}

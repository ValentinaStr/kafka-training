package com.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// Declares order-events with 3 partitions instead of Kafka's auto-create default (1).
// Spring's KafkaAdmin picks up NewTopic beans at
// startup and creates any topic that doesn't exist yet on the broker.
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderEventsTopic(
            @Value("${app.kafka.topics.order-events}") String topic,
            @Value("${app.kafka.topics.order-events-partitions}") int partitions) {
        return TopicBuilder.name(topic)
                .partitions(partitions)
                .replicas(1)
                .build();
    }
}

package com.orderservice.config;

import com.orderservice.dto.InventoryResult;
import com.orderservice.dto.OrderEvent;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .version("v1")
                        .description("REST API for creating orders and tracking their status as they flow "
                                + "through inventory checks over Kafka."));
    }

    @Bean
    public OpenApiCustomizer kafkaEventSchemasCustomizer() {
        return openApi -> {
            addSchema(openApi, OrderEvent.class);
            addSchema(openApi, InventoryResult.class);
        };
    }

    private void addSchema(OpenAPI openApi, Class<?> type) {
        ResolvedSchema resolved = ModelConverters.getInstance().resolveAsResolvedSchema(new AnnotatedType(type));
        openApi.getComponents().addSchemas(resolved.schema.getName(), resolved.schema);
        resolved.referencedSchemas.forEach(openApi.getComponents()::addSchemas);
    }
}

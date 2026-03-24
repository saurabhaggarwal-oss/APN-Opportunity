package com.ttn.ck.apn.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ message producer for the opportunity refresh flow.
 * Publishes {@link RefreshMessage} instances to the configured
 * exchange with the appropriate routing key.
 */

@Slf4j
@Component
public class OpportunityRefreshProducer {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;
    private final ObjectMapper objectMapper;

    public OpportunityRefreshProducer(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.exchange.opportunity}") String exchangeName,
            @Value("${app.rabbitmq.routing-key.opportunity-refresh}") String routingKey, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes a refresh message to the opportunity exchange.
     *
     * @param message the refresh message containing UUID and metadata
     */
    public void sendRefreshMessage(RefreshMessage message) throws JsonProcessingException {
        log.info("Publishing refresh message for: {}", message);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, objectMapper.writeValueAsString(message));
        log.debug("Message published successfully for: {}", message);
    }
}

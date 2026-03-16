package com.ttn.ck.apn.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ message producer for the opportunity refresh flow.
 * Publishes {@link RefreshMessage} instances to the configured
 * exchange with the appropriate routing key.
 */
@Component
public class OpportunityRefreshProducer {

    private static final Logger log = LoggerFactory.getLogger(OpportunityRefreshProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;

    public OpportunityRefreshProducer(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.exchange.opportunity}") String exchangeName,
            @Value("${app.rabbitmq.routing-key.opportunity-refresh}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    /**
     * Publishes a refresh message to the opportunity exchange.
     *
     * @param message the refresh message containing UUID and metadata
     */
    public void sendRefreshMessage(RefreshMessage message) {
        log.info("Publishing refresh message for UUID: {}", message.getUuid());
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
        log.debug("Message published successfully for UUID: {}", message.getUuid());
    }
}

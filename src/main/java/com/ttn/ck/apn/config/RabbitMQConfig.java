package com.ttn.ck.apn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ infrastructure configuration.
 *
 * <p>Sets up:
 * <ul>
 *   <li><b>Queue:</b> {@code opportunity-refresh-queue} — durable, with DLQ support</li>
 *   <li><b>Exchange:</b> {@code opportunity-exchange} — direct exchange</li>
 *   <li><b>Binding:</b> routes messages with key {@code opportunity.refresh} to the queue</li>
 *   <li><b>Dead Letter Queue:</b> {@code opportunity-refresh-dlq} for failed messages</li>
 *   <li><b>JSON serialization:</b> via Jackson for message conversion</li>
 * </ul>
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.queue.opportunity-refresh}")
    private String queueName;

    @Value("${app.rabbitmq.exchange.opportunity}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key.opportunity-refresh}")
    private String routingKey;

    // ── Dead Letter Queue ────────────────────────────────────────────────

    /**
     * Dead letter queue for messages that fail all retry attempts.
     * Allows manual inspection and reprocessing of failed messages.
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(queueName + ".dlq").build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(exchangeName + ".dlx");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(routingKey + ".dlq");
    }

    // ── Main Queue ───────────────────────────────────────────────────────

    /**
     * Main processing queue with dead-letter routing configured.
     * Messages that exhaust retry attempts are routed to the DLQ.
     */
    @Bean
    public Queue opportunityRefreshQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", exchangeName + ".dlx")
                .withArgument("x-dead-letter-routing-key", routingKey + ".dlq")
                .build();
    }

    @Bean
    public DirectExchange opportunityExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding opportunityRefreshBinding() {
        return BindingBuilder
                .bind(opportunityRefreshQueue())
                .to(opportunityExchange())
                .with(routingKey);
    }

    // ── Message Converter ────────────────────────────────────────────────

    /**
     * Jackson-based JSON message converter for RabbitMQ.
     * Ensures messages are serialized/deserialized as JSON.
     */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * Customized RabbitTemplate with JSON message converter.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        template.setExchange(exchangeName);
        template.setRoutingKey(routingKey);
        return template;
    }
}

package com.ttn.ck.apn.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
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

@EnableRabbit
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange.opportunity}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue.opportunity-refresh}")
    private String queueName;

    @Value("${app.rabbitmq.routing-key.opportunity-refresh}")
    private String routingKey;

    @Value("${app.rabbitmq.queue.master-data-refresh}")
    private String masterDataQueue;


    @Value("${app.rabbitmq.routing-key.master-refresh}")
    private String masterRefreshKey;

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

    // ── APN opportunity Queue ───────────────────────────────────────────────────────

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
    @Bean
    public Queue masterDataRefreshQueue() {
        return QueueBuilder.durable(masterDataQueue)
                .withArgument("x-dead-letter-exchange", exchangeName + ".dlx")
                .withArgument("x-dead-letter-routing-key", routingKey + ".dlq")
                .build();
    }

    @Bean
    public Binding masterDataRefreshBinding() {
        return BindingBuilder
                .bind(masterDataRefreshQueue())
                .to(opportunityExchange())
                .with(masterRefreshKey);
    }

}

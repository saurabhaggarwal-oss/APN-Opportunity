package com.ttn.ck.apn.service;

import com.ttn.ck.apn.model.WorkloadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.opportunity}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key.opportunity-refresh}")
    private String routingKey;

    public void publishWorkloadEvent(WorkloadEvent event) {
        log.info("Publishing WorkloadEvent for customer: {}", event.getCustomerName());
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
    }
}

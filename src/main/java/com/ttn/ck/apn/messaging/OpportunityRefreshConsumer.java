package com.ttn.ck.apn.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttn.ck.apn.service.WorkloadGenerationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer that listens to the opportunity refresh queue.
 *
 * <p>Processing flow for each message:
 * <ol>
 *   <li>Receive {@link RefreshMessage} with a UUID</li>
 *   <li>Fetch all raw data records for that UUID from Snowflake</li>
 *   <li>Send raw data through ChatGPT via {@link WorkloadGenerationService}</li>
 *   <li>Upsert the generated master data back into Snowflake</li>
 * </ol>
 *
 * <p><b>Retry behavior:</b> Spring's retry is configured in application.yaml
 * (3 attempts, exponential backoff). Messages that exhaust retries are
 * routed to the dead-letter queue for manual inspection.</p>
 */

@Slf4j
@Component
@AllArgsConstructor
public class OpportunityRefreshConsumer {

    private final ObjectMapper mapper;
    private final WorkloadGenerationService workloadGenerationService;

    /**
     * Processes a single refresh message from the queue.
     *
     * <p>Each message carries one UUID, enabling:
     * <ul>
     *   <li>Independent processing — failure of one UUID doesn't affect others</li>
     *   <li>Granular retries — only the failed UUID is retried</li>
     *   <li>Parallelism — multiple consumers can process concurrently</li>
     * </ul>
     *
     * @param message the refresh message containing the UUID to process
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.opportunity-refresh}")
    public void handleRefreshMessage(String message) throws JsonProcessingException {
        workloadGenerationService.processUnprocessedWorkloads(mapper.readValue(message, new TypeReference<>() {}));
    }
}

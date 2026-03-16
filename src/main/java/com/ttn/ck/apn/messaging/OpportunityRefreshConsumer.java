package com.ttn.ck.apn.messaging;

import com.ttn.ck.apn.dao.ApnOpportunityDataDao;
import com.ttn.ck.apn.model.ApnOpportunityMasterData;
import com.ttn.ck.apn.model.ApnOpportunityRawData;
import com.ttn.ck.apn.service.WorkloadGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

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
@Component
public class OpportunityRefreshConsumer {

    private static final Logger log = LoggerFactory.getLogger(OpportunityRefreshConsumer.class);

    private final ApnOpportunityDataDao dao;
    private final WorkloadGenerationService workloadGenerationService;

    public OpportunityRefreshConsumer(
            ApnOpportunityDataDao dao,
            WorkloadGenerationService workloadGenerationService) {
        this.dao = dao;
        this.workloadGenerationService = workloadGenerationService;
    }

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
    public void handleRefreshMessage(RefreshMessage message) {
        String uuid = message.getUuid();
        log.info("▶ Processing refresh for UUID: {} (attempt: {})",
                uuid, message.getRetryCount() + 1);

        try {
            // Step 1: Fetch raw data for this UUID
            List<ApnOpportunityRawData> rawDataList = dao.findRawDataByUuid(uuid);

            if (rawDataList.isEmpty()) {
                log.warn("No raw data found for UUID: {}. Skipping.", uuid);
                return;
            }

            log.info("  Found {} raw records for UUID: {}", rawDataList.size(), uuid);

            // Step 2: Process through ChatGPT
            List<ApnOpportunityMasterData> generatedData =
                    workloadGenerationService.processRawData(rawDataList);

            if (generatedData.isEmpty()) {
                log.warn("ChatGPT returned no master data for UUID: {}", uuid);
                return;
            }

            // Step 3: Upsert into master data table
            int upsertCount = 0;
            for (ApnOpportunityMasterData masterData : generatedData) {
                int result = dao.upsertMasterData(masterData);
                upsertCount += result;
            }

            log.info("✓ Refresh complete for UUID: {}. Upserted {} records.", uuid, upsertCount);

        } catch (Exception e) {
            log.error("✗ Refresh failed for UUID: {}. Error: {}", uuid, e.getMessage(), e);
            // Rethrow to trigger Spring's retry mechanism.
            // After max retries, the message goes to the DLQ.
            throw e;
        }
    }
}

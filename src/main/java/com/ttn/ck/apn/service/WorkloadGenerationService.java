package com.ttn.ck.apn.service;


import com.ttn.ck.apn.messaging.RefreshMessage;

/**
 * Service interface for ChatGPT-based workload generation.
 * Transforms raw opportunity data into structured master opportunity
 * data using the predefined prompt template.
 */
public interface WorkloadGenerationService {

    /**
     * Fetch unprocessed raw data records, group them by customer, process through ChatGPT in batches,
     * update the raw table, and publish a workload event.
     */
    void processUnprocessedWorkloads(RefreshMessage message);
}

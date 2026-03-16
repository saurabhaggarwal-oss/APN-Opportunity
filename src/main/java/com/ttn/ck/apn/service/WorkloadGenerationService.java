package com.ttn.ck.apn.service;

import com.ttn.ck.apn.model.ApnOpportunityMasterData;
import com.ttn.ck.apn.model.ApnOpportunityRawData;

import java.util.List;

/**
 * Service interface for ChatGPT-based workload generation.
 * Transforms raw opportunity data into structured master opportunity
 * data using the predefined prompt template.
 */
public interface WorkloadGenerationService {

    /**
     * Process a batch of raw data records through ChatGPT to generate
     * or update corresponding master opportunity data.
     *
     * @param rawDataList list of raw records to process
     * @return list of generated/updated master data records
     */
    List<ApnOpportunityMasterData> processRawData(List<ApnOpportunityRawData> rawDataList);
}

package com.ttn.ck.apn.dao;

import com.ttn.ck.apn.model.ApnOpportunityMasterData;
import com.ttn.ck.apn.model.ApnOpportunityRawData;

import java.util.List;
import java.util.Set;

/**
 * Data Access Object for APN Opportunity data.
 * Handles all database operations for both master and raw data tables
 * using native Snowflake queries via JdbcTemplate.
 */
public interface ApnOpportunityDataDao {

    /**
     * Fetch master data filtered by raised status and date range.
     * Returns all matching records (no pagination).
     *
     * @param startDate          start of the date range filter
     * @param endDate            end of the date range filter
     * @param opportunityRaised  filter by raised status (true/false)
     * @return list of matching master data records
     */
    List<ApnOpportunityMasterData> findMasterDataByFilters(String startDate, String endDate, Boolean opportunityRaised);

    /**
     * Fetch master data records by a list of UUIDs.
     * Used for the export functionality.
     *
     * @param uuids list of lineitem UUIDs to fetch
     * @return list of matching master data records
     */
    List<ApnOpportunityMasterData> findMasterDataByUuids(List<String> uuids);

    /**
     * Update the opportunity raised status, date, and user for a given UUID.
     *
     * @param uuids        the lineitem UUIDs to update
     * @param raised      the new raised status
     */
    void updateOpportunityRaised(Set<String> uuids, Boolean raised);

    /**
     * Fetch all raw data records for a given UUID.
     *
     * @param uuid the lineitem UUID to look up
     * @return list of raw data records
     */
    List<ApnOpportunityRawData> findRawDataByUuid(String uuid);

    /**
     * Fetch all unprocessed raw data records that do not have workload details generated yet.
     * 
     * @return list of unprocessed raw data records
     */
    List<ApnOpportunityRawData> fetchUnprocessedRawData();

    /**
     * Update the workload title and description for a specific raw data lineitem.
     * 
     * @param lineitemUuid the lineitem UUID
     * @param workloadTitle the generated workload title
     * @param workloadDescription the generated workload description
     */
    void updateWorkloadDetailsByLineItemUuid(String lineitemUuid, String workloadTitle, String workloadDescription);

    /**
     * Insert into opportunity master table based on the processed event data.
     * 
     * @param customerName the customer name
     * @param partnerName the partner name
     * @param workloadDescription the workload description as per requirement
     */
    void insertOpportunityMasterData(String customerName, String partnerName, String workloadDescription);

    /**
     * Insert into opportunity mapping table based on the processed event data.
     * 
     * @param customerName the customer name
     * @param partnerName the partner name
     * @param workloadDescription the workload description as per requirement
     */
    void insertOpportunityMappingData(String customerName, String partnerName, String workloadDescription);

}

package com.ttn.ck.apn.dao;

import com.ttn.ck.apn.model.ApnOpportunityMasterData;
import com.ttn.ck.apn.model.ApnOpportunityRawData;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for APN Opportunity data.
 * Handles all database operations for both master and raw data tables
 * using native Snowflake queries via JdbcTemplate.
 */
public interface ApnOpportunityDataDao {

    // ── Master Data Queries ──────────────────────────────────────────────

    /**
     * Fetch master data filtered by raised status and date range.
     * Returns all matching records (no pagination).
     *
     * @param startDate          start of the date range filter
     * @param endDate            end of the date range filter
     * @param opportunityRaised  filter by raised status (true/false)
     * @return list of matching master data records
     */
    List<ApnOpportunityMasterData> findMasterDataByFilters(
            Date startDate, Date endDate, Boolean opportunityRaised);

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
     * @param uuid        the lineitem UUID to update
     * @param raised      the new raised status
     * @param raisedDate  timestamp when the opportunity was raised (null if clearing)
     * @param raisedBy    user who raised the opportunity (null if clearing)
     * @return number of rows updated (should be 1)
     */
    int updateOpportunityRaised(String uuid, Boolean raised, Date raisedDate, String raisedBy);

    /**
     * Upsert (insert or update) a master data record.
     * Used by the refresh job after ChatGPT processing.
     *
     * @param data the master data to upsert
     * @return number of rows affected
     */
    int upsertMasterData(ApnOpportunityMasterData data);

    // ── Raw Data Queries ─────────────────────────────────────────────────

    /**
     * Fetch all raw data records for a given UUID.
     *
     * @param uuid the lineitem UUID to look up
     * @return list of raw data records
     */
    List<ApnOpportunityRawData> findRawDataByUuid(String uuid);

    /**
     * Fetch all raw data records (used by the refresh job).
     *
     * @return all raw data records
     */
    List<ApnOpportunityRawData> findAllRawData();

    /**
     * Fetch distinct UUIDs from raw data that need processing.
     *
     * @return list of distinct lineitem UUIDs
     */
    List<String> findDistinctRawDataUuids();
}

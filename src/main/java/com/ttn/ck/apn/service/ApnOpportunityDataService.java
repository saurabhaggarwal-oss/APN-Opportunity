package com.ttn.ck.apn.service;

import com.ttn.ck.apn.dto.MasterDataFilterRequest;
import com.ttn.ck.apn.dto.OpportunityData;
import com.ttn.ck.apn.dto.RaiseOpportunityRequest;
import com.ttn.ck.apn.model.ApnOpportunityRawData;

import java.util.List;

/**
 * Service interface for APN Opportunity data operations.
 * Encapsulates all business logic for opportunity listing, export,
 * raising, and refresh trigger.
 */
public interface ApnOpportunityDataService {

    /**
     * Fetch master data filtered by date range and raised status.
     *
     * @param request filter criteria (startDate, endDate, opportunityRaised)
     * @return list of matching master data records
     */
    List<OpportunityData> listMasterDataByStatus(MasterDataFilterRequest request);

    /**
     * Fetch master data records by UUIDs for export.
     *
     * @param uuids list of lineitem UUIDs
     * @return Excel file as byte array
     */
    byte[] exportMasterData(List<String> uuids);

    /**
     * Fetch raw data records for a given UUID.
     *
     * @param uuid the lineitem UUID
     * @return list of raw data records
     */
    List<ApnOpportunityRawData> getRawDataByUuid(String uuid);

    /**
     * Fetch raw data records for a UUID and export as Excel.
     *
     * @param uuid the lineitem UUID
     * @return Excel file as byte array
     */
    byte[] exportRawData(String uuid);

    /**
     * Raise or clear an opportunity status.
     *
     * @param request contains uuid, raised flag, and user info
     */
    void raiseOpportunity(RaiseOpportunityRequest request);

    /**
     * Trigger asynchronous refresh by publishing to RabbitMQ.
     * Reads raw data UUIDs and sends them to the processing queue.
     */
    void triggerRefresh();
}

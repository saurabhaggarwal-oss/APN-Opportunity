package com.ttn.ck.apn.service.impl;

import com.ttn.ck.apn.dao.ApnOpportunityDataDao;
import com.ttn.ck.apn.dto.MasterDataFilterRequest;
import com.ttn.ck.apn.dto.OpportunityData;
import com.ttn.ck.apn.dto.RaiseOpportunityRequest;
import com.ttn.ck.apn.messaging.OpportunityRefreshProducer;
import com.ttn.ck.apn.messaging.RefreshMessage;
import com.ttn.ck.apn.model.ApnOpportunityMasterData;
import com.ttn.ck.apn.model.ApnOpportunityRawData;
import com.ttn.ck.apn.service.ApnOpportunityDataService;
import com.ttn.ck.apn.service.ExcelExportService;
import com.ttn.ck.errorhandler.exceptions.GenericStatusException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Primary business logic implementation for APN Opportunity operations.
 *
 * <p>Handles listing, export, raise/clear operations, and triggers
 * asynchronous refresh by publishing messages to RabbitMQ.</p>
 */
@Slf4j
@Service
@AllArgsConstructor
public class ApnOpportunityDataServiceImpl implements ApnOpportunityDataService {

    private final ApnOpportunityDataDao dao;
    private final ExcelExportService excelExportService;
    private final OpportunityRefreshProducer refreshProducer;

    /**
     * Fetches master data filtered by raised status and date range.
     * Parses date strings from the request DTO and delegates to the DAO.
     *
     * @param request filter criteria
     * @return list of matching records
     * @throws IllegalArgumentException if date format is invalid
     */
    @Override
    public List<OpportunityData> listMasterDataByStatus(MasterDataFilterRequest request) {
        log.info("Listing master data: startDate={}, endDate={}, raised={}",
                request.getStartDate(), request.getEndDate(), request.getOpportunityRaised());
        List<ApnOpportunityMasterData> data = dao.findMasterDataByFilters(request.getStartDate(), request.getEndDate(), request.getOpportunityRaised());
        log.info("data {}", data.size());
        return new ArrayList<>();
    }

    /**
     * Fetches records by UUIDs and generates an Excel file.
     *
     * @param uuids list of UUIDs selected by the frontend
     * @return Excel file bytes
     * @throws GenericStatusException if no records found for the given UUIDs
     */
    @Override
    public byte[] exportMasterData(List<String> uuids) {
        log.info("Exporting master data for {} UUIDs", uuids.size());
        List<ApnOpportunityMasterData> records = dao.findMasterDataByUuids(uuids);
        if (records.isEmpty()) {
            throw new GenericStatusException("No master data records found for the provided UUIDs", HttpStatus.BAD_REQUEST.value());
        }

        return excelExportService.exportMasterData(records);
    }


    /**
     * Fetches all raw data records for a given UUID.
     *
     * @param uuid the lineitem UUID
     * @return list of raw data records
     * @throws GenericStatusException if no records found
     */
    @Override
    public List<ApnOpportunityRawData> getRawDataByUuid(String uuid) {
        log.info("Fetching raw data for UUID: {}", uuid);
        List<ApnOpportunityRawData> records = dao.findRawDataByUuid(uuid);
        if (records.isEmpty()) {
            throw new GenericStatusException("No data records found", HttpStatus.BAD_REQUEST.value());
        }
        return records;
    }

    /**
     * Fetches raw data for a UUID and generates an Excel file.
     *
     * @param uuid the lineitem UUID
     * @return Excel file bytes
     */
    @Override
    public byte[] exportRawData(String uuid) {
        log.info("Exporting raw data for UUID: {}", uuid);
        List<ApnOpportunityRawData> records = getRawDataByUuid(uuid);
        if (records.isEmpty()) {
            throw new GenericStatusException("No raw data records found for UUID: " + uuid, HttpStatus.BAD_REQUEST.value());
        }
        return excelExportService.exportRawData(records);
    }

    /**
     * Updates the opportunity raised status for a given UUID.
     *
     * <p>If raised = true:
     * - Sets opportunity_raised_date = current timestamp
     * - Sets opportunity_raised_by = the requesting user
     *
     * <p>If raised = false:
     * - Clears raised date and user fields
     *
     * @param request the raise/clear request DTO
     */
    @Override
    public void raiseOpportunity(RaiseOpportunityRequest request) {
        log.info("Raise opportunity: uuid={}, raised={}", request.getUuid(), request.getRaised());
        dao.updateOpportunityRaised(request.getUuid(), request.getRaised());
        log.info("Opportunity {} successfully {}", request.getUuid(), Boolean.TRUE.equals(request.getRaised()) ? "raised" : "cleared");
    }

    /**
     * Triggers an asynchronous refresh by sending distinct raw data UUIDs
     * to the RabbitMQ queue. Each UUID is processed independently by the
     * consumer, enabling parallel and retryable processing.
     */
    @Override
    public void triggerRefresh(String triggeredAt, String partnerName) {
        log.info("Triggering opportunity refresh job");
        RefreshMessage message = getRefreshMessage(triggeredAt, partnerName);
        refreshProducer.sendRefreshMessage(message);
        log.info("All refresh messages published successfully");
    }

    private RefreshMessage getRefreshMessage(String triggeredAt, String partnerName) {
        return RefreshMessage
                .builder()
                .triggeredAt(triggeredAt)
                .partnerName(partnerName)
                .build();
    }

}

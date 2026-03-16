package com.ttn.ck.apn.service;

import com.ttn.ck.apn.dao.ApnOpportunityDataDao;
import com.ttn.ck.apn.dto.MasterDataFilterRequest;
import com.ttn.ck.apn.dto.RaiseOpportunityRequest;
import com.ttn.ck.apn.exception.ResourceNotFoundException;
import com.ttn.ck.apn.messaging.OpportunityRefreshProducer;
import com.ttn.ck.apn.messaging.RefreshMessage;
import com.ttn.ck.apn.model.ApnOpportunityMasterData;
import com.ttn.ck.apn.model.ApnOpportunityRawData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Primary business logic implementation for APN Opportunity operations.
 *
 * <p>Handles listing, export, raise/clear operations, and triggers
 * asynchronous refresh by publishing messages to RabbitMQ.</p>
 */
@Service
public class ApnOpportunityDataServiceImpl implements ApnOpportunityDataService {

    private static final Logger log = LoggerFactory.getLogger(ApnOpportunityDataServiceImpl.class);
    private static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd");

    private final ApnOpportunityDataDao dao;
    private final ExcelExportService excelExportService;
    private final OpportunityRefreshProducer refreshProducer;

    public ApnOpportunityDataServiceImpl(
            ApnOpportunityDataDao dao,
            ExcelExportService excelExportService,
            OpportunityRefreshProducer refreshProducer) {
        this.dao = dao;
        this.excelExportService = excelExportService;
        this.refreshProducer = refreshProducer;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  1. Listing by Status
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Fetches master data filtered by raised status and date range.
     * Parses date strings from the request DTO and delegates to the DAO.
     *
     * @param request filter criteria
     * @return list of matching records
     * @throws IllegalArgumentException if date format is invalid
     */
    @Override
    public List<ApnOpportunityMasterData> listMasterDataByStatus(MasterDataFilterRequest request) {
        log.info("Listing master data: startDate={}, endDate={}, raised={}",
                request.getStartDate(), request.getEndDate(), request.getOpportunityRaised());

        Date startDate = parseDate(request.getStartDate());
        Date endDate = parseDate(request.getEndDate());

        // Validate date range
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }

        return dao.findMasterDataByFilters(startDate, endDate, request.getOpportunityRaised());
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  2. Export Master Data
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Fetches records by UUIDs and generates an Excel file.
     *
     * @param uuids list of UUIDs selected by the frontend
     * @return Excel file bytes
     * @throws ResourceNotFoundException if no records found for the given UUIDs
     */
    @Override
    public byte[] exportMasterData(List<String> uuids) {
        log.info("Exporting master data for {} UUIDs", uuids.size());

        List<ApnOpportunityMasterData> records = dao.findMasterDataByUuids(uuids);

        if (records.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No master data records found for the provided UUIDs");
        }

        return excelExportService.exportMasterData(records);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  3. Raw Data by UUID
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Fetches all raw data records for a given UUID.
     *
     * @param uuid the lineitem UUID
     * @return list of raw data records
     * @throws ResourceNotFoundException if no records found
     */
    @Override
    public List<ApnOpportunityRawData> getRawDataByUuid(String uuid) {
        log.info("Fetching raw data for UUID: {}", uuid);

        List<ApnOpportunityRawData> records = dao.findRawDataByUuid(uuid);

        if (records.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No raw data records found for UUID: " + uuid);
        }

        return records;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  4. Export Raw Data
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Fetches raw data for a UUID and generates an Excel file.
     *
     * @param uuid the lineitem UUID
     * @return Excel file bytes
     */
    @Override
    public byte[] exportRawData(String uuid) {
        log.info("Exporting raw data for UUID: {}", uuid);

        List<ApnOpportunityRawData> records = dao.findRawDataByUuid(uuid);

        if (records.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No raw data records found for UUID: " + uuid);
        }

        return excelExportService.exportRawData(records);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  5. Raise Opportunity
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Updates the opportunity raised status for a given UUID.
     *
     * <p>If raised = true:
     *   - Sets opportunity_raised_date = current timestamp
     *   - Sets opportunity_raised_by = the requesting user
     *
     * <p>If raised = false:
     *   - Clears raised date and user fields
     *
     * @param request the raise/clear request DTO
     * @throws IllegalArgumentException if raised=true but user is blank
     * @throws ResourceNotFoundException if UUID not found in master data
     */
    @Override
    public void raiseOpportunity(RaiseOpportunityRequest request) {
        log.info("Raise opportunity: uuid={}, raised={}, user={}",
                request.getUuid(), request.getRaised(), request.getUser());

        // Validate: user is required when raising
        if (Boolean.TRUE.equals(request.getRaised())
                && (request.getUser() == null || request.getUser().isBlank())) {
            throw new IllegalArgumentException("User is required when raising an opportunity");
        }

        Date raisedDate = null;
        String raisedBy = null;

        if (Boolean.TRUE.equals(request.getRaised())) {
            raisedDate = new Date(); // current timestamp
            raisedBy = request.getUser();
        }

        int updatedRows = dao.updateOpportunityRaised(
                request.getUuid(), request.getRaised(), raisedDate, raisedBy);

        if (updatedRows == 0) {
            throw new ResourceNotFoundException(
                    "No opportunity found with UUID: " + request.getUuid());
        }

        log.info("Opportunity {} successfully {}",
                request.getUuid(), request.getRaised() ? "raised" : "cleared");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  6. Trigger Refresh
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Triggers an asynchronous refresh by sending distinct raw data UUIDs
     * to the RabbitMQ queue. Each UUID is processed independently by the
     * consumer, enabling parallel and retryable processing.
     */
    @Override
    public void triggerRefresh() {
        log.info("Triggering opportunity refresh job");

        List<String> uuids = dao.findDistinctRawDataUuids();

        if (uuids.isEmpty()) {
            log.warn("No raw data records found for refresh");
            return;
        }

        log.info("Publishing {} UUIDs to refresh queue", uuids.size());

        for (String uuid : uuids) {
            RefreshMessage message = RefreshMessage.builder()
                    .uuid(uuid)
                    .triggeredAt(new Date())
                    .build();
            refreshProducer.sendRefreshMessage(message);
        }

        log.info("All refresh messages published successfully");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Utility
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Parses a date string in yyyy-MM-dd format.
     *
     * @param dateStr the date string to parse
     * @return parsed Date object
     * @throws IllegalArgumentException if format is invalid
     */
    private Date parseDate(String dateStr) {
        try {
            return DATE_PARSER.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format: '" + dateStr + "'. Expected: yyyy-MM-dd", e);
        }
    }
}

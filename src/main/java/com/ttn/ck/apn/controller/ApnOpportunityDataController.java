package com.ttn.ck.apn.controller;

import com.ttn.ck.apn.dto.*;
import com.ttn.ck.apn.model.ApnOpportunityRawData;
import com.ttn.ck.apn.service.ApnOpportunityDataService;
import com.ttn.ck.core.response.SuccessResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for APN Opportunity data operations.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET  /apn-opportunities/master}              — Listing by status</li>
 *   <li>{@code POST /apn-opportunities/master/export}       — Export master data to Excel</li>
 *   <li>{@code GET  /apn-opportunities/raw/{uuid}}          — Fetch raw data by UUID</li>
 *   <li>{@code GET  /apn-opportunities/raw/export/{uuid}}   — Export raw data to Excel</li>
 *   <li>{@code POST /apn-opportunities/raise}               — Raise/clear opportunity</li>
 *   <li>{@code POST /apn-opportunities/refresh}             — Trigger async refresh</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/apn-opportunities")
@RequiredArgsConstructor
public class ApnOpportunityDataController {

    private final ApnOpportunityDataService service;

    /**
     * Fetches master opportunity data filtered by raised status and date range.
     *
     * <p>Example:
     * {@code GET /apn-opportunities/master?startDate=2026-01-01&endDate=2026-03-12&opportunityRaised=true}
     *
     * @param startDate          start of date range (yyyy-MM-dd)
     * @param endDate            end of date range (yyyy-MM-dd)
     * @param opportunityRaised  filter by raised status
     * @return list of matching master data records
     */
    @GetMapping("/master")
    public SuccessResponseDto<List<OpportunityData>> listMasterDataByStatus(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam Boolean opportunityRaised) {

        log.info("GET /master — startDate={}, endDate={}, raised={}", startDate, endDate, opportunityRaised);
        MasterDataFilterRequest request = MasterDataFilterRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .opportunityRaised(opportunityRaised)
                .build();

        return new SuccessResponseDto<>(service.listMasterDataByStatus(request));
    }

    /**
     * Exports selected master data records to an Excel file.
     *
     * <p>Example request body:
     * <pre>
     * {
     *   "uuids": [
     *     "550e8400-e29b-41d4-a716-446655440000",
     *     "6ba7b810-9dad-11d1-80b4-00c04fd430c8"
     *   ]
     * }
     * </pre>
     *
     * @param request list of UUIDs to export
     * @return Excel file download
     */
    @PostMapping("/master/export")
    public ResponseEntity<byte[]> exportMasterData(@Valid @RequestBody ExportRequest request) {
        log.info("POST /master/export — {} UUIDs", request.getUuids().size());

        byte[] excelFile = service.exportMasterData(request.getUuids());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=master_opportunity_data.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelFile.length)
                .body(excelFile);
    }

    /**
     * Fetches all raw opportunity data records for a given UUID.
     *
     * <p>Example:
     * {@code GET /apn-opportunities/raw/550e8400-e29b-41d4-a716-446655440000}
     *
     * @param uuid the lineitem UUID
     * @return list of raw data records
     */
    @GetMapping("/raw/{uuid}")
    public SuccessResponseDto<List<ApnOpportunityRawData>> getRawData(@PathVariable String uuid) {
        return new SuccessResponseDto<>(service.getRawDataByUuid(uuid));
    }


    /**
     * Exports raw data for a UUID to an Excel file.
     *
     * <p>Example:
     * {@code GET /apn-opportunities/raw/export/550e8400-e29b-41d4-a716-446655440000}
     *
     * @param uuid the lineitem UUID
     * @return Excel file download
     */
    @GetMapping("/raw/export/{uuid}")
    public ResponseEntity<byte[]> exportRawData(@PathVariable String uuid) {
        log.info("GET /raw/export/{}", uuid);

        byte[] excelFile = service.exportRawData(uuid);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=raw_opportunity_data_" + uuid + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelFile.length)
                .body(excelFile);
    }

    /**
     * Raises or clears an opportunity status.
     *
     * <p>Example request body:
     * <pre>
     * {
     *   "uuid": "550e8400-e29b-41d4-a716-446655440000",
     *   "raised": true,
     *   "user": "saurabh.kumar"
     * }
     * </pre>
     *
     * @param request the raise/clear request
     * @return success response
     */
    @PostMapping("/raise")
    public SuccessResponseDto<Boolean> raiseOpportunity(@Valid @RequestBody RaiseOpportunityRequest request) {

        log.info("POST /raise — uuid={}, raised={}", request.getUuid(), request.getRaised());
        service.raiseOpportunity(request);
        return new SuccessResponseDto<>(Boolean.TRUE);
    }

    /**
     * Triggers an asynchronous refresh job.
     * Publishes messages to RabbitMQ for each distinct UUID in raw data.
     * Processing happens asynchronously via the queue consumer.
     *
     * <p>Example:
     * {@code POST /apn-opportunities/refresh}
     *
     * @return acknowledgment response
     */
    @PostMapping("/refresh")
    public SuccessResponseDto<Boolean> triggerRefresh() {
        log.info("POST /refresh — triggering async refresh");
        service.triggerRefresh();
        return new SuccessResponseDto<>(Boolean.TRUE);
    }
}

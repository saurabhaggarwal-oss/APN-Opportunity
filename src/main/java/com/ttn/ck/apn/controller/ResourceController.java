package com.ttn.ck.apn.controller;

import com.ttn.ck.apn.dto.SuccessResponseDto;
import com.ttn.ck.apn.model.Resource;
import com.ttn.ck.apn.repository.ResourceRepository;
import com.ttn.ck.apn.dto.RegroupRequest;
import com.ttn.ck.apn.service.ExcelExportService;
import com.ttn.ck.apn.service.ResourceGroupingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceRepository resourceRepository;
    private final ResourceGroupingService resourceGroupingService;
    private final ExcelExportService excelExportService;

    // ── FR-8.1 / FR-8.2: List resources with filters + search ─────────────

    @GetMapping
    public SuccessResponseDto<List<Resource>> getResources(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String search) {

        boolean hasFilters = anyPresent(customerName, productName, region, search);
        List<Resource> result = hasFilters
                ? resourceRepository.findWithFilters(
                        blankToNull(customerName), blankToNull(productName),
                        blankToNull(region), blankToNull(search))
                : resourceRepository.findAll();

        return new SuccessResponseDto<>(result);
    }

    /** Returns distinct filter values for resource page dropdowns (FR-8.1) */
    @GetMapping("/filter-options")
    public SuccessResponseDto<Map<String, List<String>>> getFilterOptions() {
        return new SuccessResponseDto<>(Map.of(
                "customers", resourceRepository.findDistinctCustomerNames(),
                "products",  resourceRepository.findDistinctProductNames(),
                "regions",   resourceRepository.findDistinctRegions()
        ));
    }

    /** Get a single resource by ARN/ID */
    @GetMapping("/{id}")
    public SuccessResponseDto<Resource> getResource(@PathVariable String id) {
        return new SuccessResponseDto<>(resourceRepository.findById(id).orElse(null));
    }

    // ── FR-3.4: Custom re-grouping ──────────────────────────────────────────

    /**
     * Triggers custom resource grouping with user-selected fields.
     * If groupingFields is null/empty, uses default logic (FR-3.1–3.3).
     */
    @PostMapping("/regroup")
    public SuccessResponseDto<String> regroupResources(@RequestBody RegroupRequest request) {
        log.info("Regroup requested with fields: {}, customer filter: {}",
                request.getGroupingFields(), request.getCustomerName());

        int resourceCount = resourceGroupingService.regroupAll(
                request.getGroupingFields(),
                request.getCustomerName());

        return new SuccessResponseDto<>(
                "Regroup complete. Processed " + resourceCount + " resources.");
    }

    // ── Export resource data as Excel ───────────────────────────────────────

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportResources(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String search) {

        boolean hasFilters = anyPresent(customerName, productName, region, search);
        List<Resource> records = hasFilters
                ? resourceRepository.findWithFilters(
                        blankToNull(customerName), blankToNull(productName),
                        blankToNull(region), blankToNull(search))
                : resourceRepository.findAll();

        byte[] excel = excelExportService.exportRawData(records);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resources_export.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excel.length)
                .body(excel);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean anyPresent(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return true;
        return false;
    }

    private String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }
}

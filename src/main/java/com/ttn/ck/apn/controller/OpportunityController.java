package com.ttn.ck.apn.controller;

import com.ttn.ck.apn.dto.SuccessResponseDto;
import com.ttn.ck.apn.model.Opportunity;
import com.ttn.ck.apn.model.Resource;
import com.ttn.ck.apn.repository.OpportunityRepository;
import com.ttn.ck.apn.repository.ResourceRepository;
import com.ttn.ck.apn.service.ExcelExportService;
import com.ttn.ck.apn.service.impl.WorkloadGenerationServiceImpl;
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
@RequestMapping("/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityRepository opportunityRepository;
    private final ResourceRepository resourceRepository;
    private final ExcelExportService excelExportService;
    private final WorkloadGenerationServiceImpl aiService;

    // ── FR-5.1: List with filters + search (FR-8) ──────────────────────────

    @GetMapping
    public SuccessResponseDto<List<Opportunity>> getAllOpportunities(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        boolean hasFilters = anyPresent(customerName, productName, region, status, search);
        List<Opportunity> result = hasFilters
                ? opportunityRepository.findWithFilters(
                        blankToNull(customerName), blankToNull(productName),
                        blankToNull(region), blankToNull(status), blankToNull(search))
                : opportunityRepository.findAll();

        return new SuccessResponseDto<>(result);
    }

    /** Returns distinct filter values for dropdowns (FR-8.1) */
    @GetMapping("/filter-options")
    public SuccessResponseDto<Map<String, List<String>>> getFilterOptions() {
        return new SuccessResponseDto<>(Map.of(
                "customers", opportunityRepository.findDistinctCustomerNames(),
                "products",  opportunityRepository.findDistinctProductNames(),
                "regions",   opportunityRepository.findDistinctRegions(),
                "statuses",  List.of("draft", "reviewed", "exported")
        ));
    }

    // ── FR-5.3: Opportunity detail ──────────────────────────────────────────

    @GetMapping("/{id}")
    public SuccessResponseDto<Opportunity> getOpportunity(@PathVariable String id) {
        return new SuccessResponseDto<>(opportunityRepository.findById(id).orElse(null));
    }

    /** List all resources that belong to a specific opportunity (FR-5.3) */
    @GetMapping("/{id}/resources")
    public SuccessResponseDto<List<Resource>> getOpportunityResources(@PathVariable String id) {
        return new SuccessResponseDto<>(resourceRepository.findByGroupKey(id));
    }

    // ── FR-5.2: Edit opportunity ────────────────────────────────────────────

    @PutMapping("/{id}")
    public SuccessResponseDto<Opportunity> updateOpportunity(
            @PathVariable String id,
            @RequestBody Opportunity updatedOpp) {

        Opportunity exp = opportunityRepository.findById(id).orElseThrow();
        exp.setTitle(updatedOpp.getTitle());
        exp.setDescription(updatedOpp.getDescription());
        exp.setOpportunityType(updatedOpp.getOpportunityType());
        exp.setUseCase(updatedOpp.getUseCase());
        exp.setStatus(updatedOpp.getStatus());
        if (updatedOpp.getEstimatedMrr() != null) {
            exp.setEstimatedMrr(updatedOpp.getEstimatedMrr()); // FR-5.2 fix
        }
        return new SuccessResponseDto<>(opportunityRepository.save(exp));
    }

    // ── FR-4.6 / FR-4.7: AI generation ────────────────────────────────────

    /** Generate AI titles for selected opportunities (FR-4.6) */
    @PostMapping("/generate-ai")
    public SuccessResponseDto<String> generateAi(@RequestBody Map<String, List<String>> body) {
        List<String> uuids = body.get("ids");
        if (uuids != null && !uuids.isEmpty()) {
            aiService.generateOpportuntiesTitles(uuids);
        }
        return new SuccessResponseDto<>("AI generation initiated for " + (uuids != null ? uuids.size() : 0) + " opportunities.");
    }

    /** Regenerate titles for ALL opportunities (FR-4.7) */
    @PostMapping("/generate-ai/all")
    public SuccessResponseDto<String> generateAiAll() {
        List<String> allIds = opportunityRepository.findAll()
                .stream()
                .map(Opportunity::getGroupKey)
                .toList();
        if (!allIds.isEmpty()) {
            aiService.generateOpportuntiesTitles(allIds);
        }
        return new SuccessResponseDto<>("Regenerating AI titles for all " + allIds.size() + " opportunities.");
    }

    // ── FR-6: APN CSV Export ────────────────────────────────────────────────

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportOpportunities(@RequestBody Map<String, List<String>> body) {
        List<String> uuids = body.get("ids");
        List<Opportunity> records = (uuids != null && !uuids.isEmpty())
                ? opportunityRepository.findAllById(uuids)
                : opportunityRepository.findAll();

        // Mark as exported (FR-6.5)
        records.forEach(r -> r.setStatus("exported"));
        opportunityRepository.saveAll(records);

        // Generate APN CSV (FR-6.1–6.4)
        byte[] csv = excelExportService.exportApnCsv(records);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=apn_opportunities_export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csv.length)
                .body(csv);
    }

    /** Export internal opportunity view as Excel (for internal use) */
    @PostMapping("/export/excel")
    public ResponseEntity<byte[]> exportOpportunitiesExcel(@RequestBody Map<String, List<String>> body) {
        List<String> uuids = body.get("ids");
        List<Opportunity> records = (uuids != null && !uuids.isEmpty())
                ? opportunityRepository.findAllById(uuids)
                : opportunityRepository.findAll();

        byte[] excel = excelExportService.exportMasterData(records);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=apn_opportunities_internal.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
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

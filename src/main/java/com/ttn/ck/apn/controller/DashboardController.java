package com.ttn.ck.apn.controller;

import com.ttn.ck.apn.dto.CustomerDetail;
import com.ttn.ck.apn.dto.CustomerSummaryDto;
import com.ttn.ck.apn.dto.DashboardSummary;
import com.ttn.ck.apn.dto.SuccessResponseDto;
import com.ttn.ck.apn.service.AnalyticsIngestionService;
import com.ttn.ck.apn.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AnalyticsIngestionService analyticsIngestionService;

    /**
     * FR-7.2 – FR-7.5: Full dashboard data (summary stats, monthly trends,
     * top-15 customers, monthly breakdown table).
     */
    @GetMapping
    public SuccessResponseDto<DashboardSummary> getDashboardData() {
        return new SuccessResponseDto<>(dashboardService.getFullDashboard());
    }

    /** FR-7.3: Monthly trends only (count + MRR per month) */
    @GetMapping("/monthly")
    public SuccessResponseDto<List<DashboardSummary.MonthlyTrend>> getMonthlyTrends() {
        return new SuccessResponseDto<>(dashboardService.getMonthlyTrends());
    }

    /** FR-7.4: Top 15 customers by MRR, marked if in pipeline */
    @GetMapping("/top-customers")
    public SuccessResponseDto<List<DashboardSummary.CustomerMrrSummary>> getTopCustomers() {
        return new SuccessResponseDto<>(dashboardService.getTopCustomers(null));
    }

    /** FR-7.6: Customer-wise analysis table with optional search */
    @GetMapping("/customers")
    public SuccessResponseDto<List<CustomerSummaryDto>> getCustomerSummaries(
            @RequestParam(required = false) String search) {
        return new SuccessResponseDto<>(dashboardService.getCustomerSummaries(search));
    }

    /** FR-7.7: Per-customer detail with monthly trend + recommended opportunities */
    @GetMapping("/customers/{name}")
    public SuccessResponseDto<CustomerDetail> getCustomerDetail(@PathVariable String name) {
        return new SuccessResponseDto<>(dashboardService.getCustomerDetail(name));
    }

    /** FR-7.1: Upload Analysis Excel to populate submitted_opportunity data */
    @PostMapping("/upload-analysis")
    public SuccessResponseDto<String> uploadAnalysis(@RequestParam("file") MultipartFile file) {
        log.info("Received Analysis Excel upload: {}", file.getOriginalFilename());
        int rows = analyticsIngestionService.processAnalysisUpload(file);
        return new SuccessResponseDto<>("Analysis data uploaded. " + rows + " records processed.");
    }
}

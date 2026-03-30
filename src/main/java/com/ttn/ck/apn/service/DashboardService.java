package com.ttn.ck.apn.service;

import com.ttn.ck.apn.dto.CustomerDetail;
import com.ttn.ck.apn.dto.CustomerSummaryDto;
import com.ttn.ck.apn.dto.DashboardSummary;
import com.ttn.ck.apn.model.Opportunity;
import com.ttn.ck.apn.repository.OpportunityRepository;
import com.ttn.ck.apn.repository.SubmittedOpportunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides all analytics data for the dashboard. FR-7.2 – FR-7.7
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int TOP_CUSTOMERS_LIMIT = 15;

    private final SubmittedOpportunityRepository submittedRepo;
    private final OpportunityRepository opportunityRepo;

    // ── FR-7.2 Summary Stats ────────────────────────────────────────────────

    public DashboardSummary getFullDashboard() {
        // Submitted stats
        Long totalOppCount = submittedRepo.sumTotalOpportunities();
        BigDecimal totalMrr = submittedRepo.sumTotalMrr();
        long distinctCustomers = submittedRepo.countDistinctCustomers();

        // New recommended (from pipeline)
        List<Opportunity> pipeline = opportunityRepo.findAll();
        long newRecCount = pipeline.size();
        BigDecimal newRecMrr = pipeline.stream()
                .filter(o -> o.getEstimatedMrr() != null)
                .map(Opportunity::getEstimatedMrr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Monthly trends (FR-7.3)
        List<DashboardSummary.MonthlyTrend> trends = getMonthlyTrends();

        // Top 15 customers (FR-7.4)
        Set<String> pipelineCustomers = pipeline.stream()
                .map(Opportunity::getCustomerName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<DashboardSummary.CustomerMrrSummary> topCustomers = getTopCustomers(pipelineCustomers);

        // Monthly breakdown (FR-7.5)
        List<DashboardSummary.MonthlyBreakdown> breakdown = getMonthlyBreakdown();

        return DashboardSummary.builder()
                .totalSubmittedOpportunities(totalOppCount != null ? totalOppCount : 0L)
                .totalSubmittedMrr(totalMrr != null ? totalMrr : BigDecimal.ZERO)
                .totalCustomersWithSubmissions(distinctCustomers)
                .newRecommendedOpportunities(newRecCount)
                .newRecommendedMrr(newRecMrr)
                .monthlyTrends(trends)
                .topCustomers(topCustomers)
                .monthlyBreakdown(breakdown)
                .build();
    }

    // ── FR-7.3 Monthly Trends ───────────────────────────────────────────────

    public List<DashboardSummary.MonthlyTrend> getMonthlyTrends() {
        List<Object[]> rows = submittedRepo.getMonthlyTrends();
        return rows.stream().map(row -> {
            int year  = toInt(row[0]);
            int month = toInt(row[1]);
            long count = toLong(row[2]);
            BigDecimal mrr = toBigDecimal(row[3]);
            return DashboardSummary.MonthlyTrend.builder()
                    .year(year)
                    .month(month)
                    .monthLabel(monthLabel(year, month))
                    .opportunityCount(count)
                    .mrr(mrr)
                    .build();
        }).collect(Collectors.toList());
    }

    // ── FR-7.4 Top 15 Customers ─────────────────────────────────────────────

    public List<DashboardSummary.CustomerMrrSummary> getTopCustomers(Set<String> pipelineCustomers) {
        List<Object[]> rows = submittedRepo.getTopCustomersByMrr();
        return rows.stream()
                .limit(TOP_CUSTOMERS_LIMIT)
                .map(row -> {
                    String name = (String) row[0];
                    long count  = toLong(row[1]);
                    BigDecimal mrr = toBigDecimal(row[2]);
                    boolean inPipeline = pipelineCustomers != null && pipelineCustomers.contains(name);
                    return DashboardSummary.CustomerMrrSummary.builder()
                            .customerName(name)
                            .opportunityCount(count)
                            .totalMrr(mrr)
                            .inPipeline(inPipeline)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ── FR-7.5 Monthly Breakdown Table ──────────────────────────────────────

    public List<DashboardSummary.MonthlyBreakdown> getMonthlyBreakdown() {
        List<Object[]> rows = submittedRepo.getMonthlyBreakdown();
        return rows.stream().map(row -> {
            int year         = toInt(row[0]);
            int month        = toInt(row[1]);
            long count       = toLong(row[2]);
            BigDecimal mrr   = toBigDecimal(row[3]);
            long activeCust  = toLong(row[4]);
            return DashboardSummary.MonthlyBreakdown.builder()
                    .year(year)
                    .month(month)
                    .monthLabel(monthLabel(year, month))
                    .opportunityCount(count)
                    .mrr(mrr)
                    .activeCustomers(activeCust)
                    .build();
        }).collect(Collectors.toList());
    }

    // ── FR-7.6 Customer-wise Analysis Table ─────────────────────────────────

    public List<CustomerSummaryDto> getCustomerSummaries(String search) {
        Set<String> pipelineCustomers = opportunityRepo.findAll().stream()
                .map(Opportunity::getCustomerName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Object[]> rows = submittedRepo.getCustomerSummaries();
        return rows.stream()
                .filter(row -> search == null || search.isBlank() ||
                        ((String) row[0]).toLowerCase().contains(search.toLowerCase()))
                .map(row -> {
                    String name  = (String) row[0];
                    long count   = toLong(row[1]);
                    BigDecimal mrr = toBigDecimal(row[2]);
                    Integer lastYear  = row[3] != null ? toInt(row[3]) : null;
                    Integer lastMonth = row[4] != null ? toInt(row[4]) : null;
                    return CustomerSummaryDto.builder()
                            .customerName(name)
                            .totalOpportunities(count)
                            .totalMrr(mrr)
                            .inPipeline(pipelineCustomers.contains(name))
                            .lastYear(lastYear)
                            .lastMonth(lastMonth)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ── FR-7.7 Per-Customer Detail ───────────────────────────────────────────

    public CustomerDetail getCustomerDetail(String customerName) {
        // Historical monthly trends for this customer
        List<Object[]> trendRows = submittedRepo.getMonthlyTrendsByCustomer(customerName);
        List<DashboardSummary.MonthlyTrend> trends = trendRows.stream().map(row -> {
            int year  = toInt(row[0]);
            int month = toInt(row[1]);
            long count = toLong(row[2]);
            BigDecimal mrr = toBigDecimal(row[3]);
            return DashboardSummary.MonthlyTrend.builder()
                    .year(year).month(month).monthLabel(monthLabel(year, month))
                    .opportunityCount(count).mrr(mrr).build();
        }).collect(Collectors.toList());

        // Totals
        long totalCount = trends.stream().mapToLong(DashboardSummary.MonthlyTrend::getOpportunityCount).sum();
        BigDecimal totalMrr = trends.stream()
                .map(DashboardSummary.MonthlyTrend::getMrr)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Recommended opportunities from pipeline for this customer
        List<Opportunity> opps = opportunityRepo.findByCustomerName(customerName);
        List<CustomerDetail.OpportunityDetail> recommended = opps.stream().map(o ->
                CustomerDetail.OpportunityDetail.builder()
                        .groupKey(o.getGroupKey())
                        .title(o.getTitle())
                        .productName(o.getProductName())
                        .region(o.getRegion())
                        .nameTag(o.getNameTag())
                        .resourceCount(o.getResourceCount())
                        .estimatedMrr(o.getEstimatedMrr())
                        .status(o.getStatus())
                        .build()
        ).collect(Collectors.toList());

        return CustomerDetail.builder()
                .customerName(customerName)
                .totalOpportunities(totalCount)
                .totalMrr(totalMrr)
                .monthlyTrends(trends)
                .recommendedOpportunities(recommended)
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String monthLabel(int year, int month) {
        String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        return monthName + " " + year;
    }

    private int toInt(Object o) {
        if (o instanceof Number n) return n.intValue();
        return 0;
    }

    private long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        return 0L;
    }

    private BigDecimal toBigDecimal(Object o) {
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }
}

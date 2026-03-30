package com.ttn.ck.apn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {

    // FR-7.2 Summary Stats
    private long totalSubmittedOpportunities;
    private BigDecimal totalSubmittedMrr;
    private long totalCustomersWithSubmissions;
    private long newRecommendedOpportunities;
    private BigDecimal newRecommendedMrr;

    // FR-7.3 Monthly trends
    private List<MonthlyTrend> monthlyTrends;

    // FR-7.4 Top 15 customers
    private List<CustomerMrrSummary> topCustomers;

    // FR-7.5 Monthly breakdown table
    private List<MonthlyBreakdown> monthlyBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        private int year;
        private int month;
        private String monthLabel; // e.g. "Jan 2025"
        private long opportunityCount;
        private BigDecimal mrr;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerMrrSummary {
        private String customerName;
        private long opportunityCount;
        private BigDecimal totalMrr;
        private boolean inPipeline; // has recommended opportunities
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyBreakdown {
        private int year;
        private int month;
        private String monthLabel;
        private long opportunityCount;
        private BigDecimal mrr;
        private long activeCustomers;
    }
}

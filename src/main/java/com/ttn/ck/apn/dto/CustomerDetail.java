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
public class CustomerDetail {

    private String customerName;
    private long totalOpportunities;
    private BigDecimal totalMrr;

    // FR-7.7 Monthly trend for this customer
    private List<DashboardSummary.MonthlyTrend> monthlyTrends;

    // FR-7.7 New recommended opportunities
    private List<OpportunityDetail> recommendedOpportunities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpportunityDetail {
        private String groupKey;
        private String title;
        private String productName;
        private String region;
        private String nameTag;
        private Integer resourceCount;
        private BigDecimal estimatedMrr;
        private String status;
    }
}

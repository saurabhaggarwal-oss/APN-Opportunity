package com.ttn.ck.apn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryDto {
    private String customerName;
    private long totalOpportunities;
    private BigDecimal totalMrr;
    private boolean inPipeline;
    private Integer lastYear;
    private Integer lastMonth;
}

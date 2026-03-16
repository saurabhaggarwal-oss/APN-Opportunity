package com.ttn.ck.apn.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for filtering master opportunity data.
 * Used by the listing API to filter by date range and raised status.
 *
 * Example request params:
 *   GET /apn-opportunities/master?startDate=2026-01-01&endDate=2026-03-12&opportunityRaised=true
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterDataFilterRequest {

    /**
     * Start date for filtering opportunity_raised_date (format: yyyy-MM-dd).
     */
    @NotNull(message = "startDate is required")
    private String startDate;

    /**
     * End date for filtering opportunity_raised_date (format: yyyy-MM-dd).
     */
    @NotNull(message = "endDate is required")
    private String endDate;

    /**
     * Filter by opportunity raised status (true/false).
     */
    @NotNull(message = "opportunityRaised status is required")
    private Boolean opportunityRaised;
}

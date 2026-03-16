package com.ttn.ck.apn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for raising or clearing an opportunity.
 *
 * Example request body:
 * {
 *   "uuid": "550e8400-e29b-41d4-a716-446655440000",
 *   "raised": true,
 *   "user": "saurabh.kumar"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaiseOpportunityRequest {

    @NotBlank(message = "UUID is required")
    private String uuid;

    @NotNull(message = "raised flag is required")
    private Boolean raised;

    /**
     * User who is raising/clearing the opportunity.
     * Required when raised = true, optional otherwise.
     */
    private String user;
}

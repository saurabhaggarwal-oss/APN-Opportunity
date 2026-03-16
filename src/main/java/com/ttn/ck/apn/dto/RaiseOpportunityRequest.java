package com.ttn.ck.apn.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for raising or clearing an opportunity.
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

    @NotEmpty(message = "UUID is required")
    private Set<String> uuid;

    @NotNull(message = "raised flag is required")
    private Boolean raised;

}

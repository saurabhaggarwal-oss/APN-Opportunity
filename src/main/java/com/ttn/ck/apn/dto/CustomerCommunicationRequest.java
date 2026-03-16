package com.ttn.ck.apn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding or updating a customer communication record.
 * Example request body:
 * {
 *   "customerName": "John Doe",
 *   "customerCompanyName": "Acme Corp",
 *   "industry": "Technology",
 *   "country": "US",
 *   "state": "CA",
 *   "postalCode": "94105",
 *   "website": "<a href="https://cloudkeeper.com">...</a>"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCommunicationRequest {

    private String uuid;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer company name is required")
    private String customerCompanyName;

    private String industry;
    private String industryOther;
    private String country;
    private String state;
    private String postalCode;
    private String website;
}

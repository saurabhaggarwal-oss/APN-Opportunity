package com.ttn.ck.apn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityData {

    private String lineitemUuid;
    private String customerName;
    private String customerCompanyName;
    private String industry;
    private String industryOther;
    private String country;
    private String state;
    private String postalCode;
    private String website;
    private String partnerPrimaryNeedFromAws;
    private String partnerProjectTitle;
    private String customerBusinessProblem;
    private String solutionOffered;
    private String otherSolutionOffered;
    private String nextApplicableSteps;
    private String useCase;
    private Double estimatedMonthlyRevenue;
    private String targetCloseDate;
    private String opportunityType;
    private String deliveryModel;
    private Boolean isOpportunityFromMarketing;
    private String salesActivity;
    private String accountId;
    private String primarySalesContactFirstName;
    private String primarySalesContactLastName;
    private String primarySalesContactPhone;
    private String primarySalesContactEmail;
    private String primarySalesContactDesignation;
    private String loggedDate;

}

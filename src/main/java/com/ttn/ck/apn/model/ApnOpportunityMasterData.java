package com.ttn.ck.apn.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Plain POJO representing a row in the Snowflake {@code apn_opportunity_master_data}
 * table.
 * No JPA annotations — mapped manually from native query results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApnOpportunityMasterData {

    private Date createdDate;
    private Date modifiedDate;
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
    private Date targetCloseDate;
    private String opportunityType;
    private String deliveryModel;
    private Boolean isOpportunityFromMarketing;
    private String salesActivity;
    private String lineitemUsageaccountid;
    private String primarySalesContactFirstName;
    private String primarySalesContactLastName;
    private String primarySalesContactPhone;
    private String primarySalesContactEmail;
    private String primarySalesContactDesignation;
    private Boolean opportunityRaised;
    private Date opportunityRaisedDate;
    private String opportunityRaisedBy;
    private Date loggedDate;
    private String cloudPlatform;
    private String partnerName;

}

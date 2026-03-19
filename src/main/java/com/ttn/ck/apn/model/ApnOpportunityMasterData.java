package com.ttn.ck.apn.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


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

    @JsonAlias("CREATED_DATE")
    private LocalDateTime createdDate;
    @JsonAlias("MODIFIED_DATE")
    private LocalDateTime modifiedDate;
    @JsonAlias("MASTER_UUID")
    private String lineitemUuid;
    @JsonAlias("CUSTOMER_NAME")
    private String customerName;
    @JsonAlias("CUSTOMER_COMPANY_NAME")
    private String customerCompanyName;
    @JsonAlias("INDUSTRY")
    private String industry;
    @JsonAlias("INDUSTRY_OTHER")
    private String industryOther;
    @JsonAlias("COUNTRY")
    private String country;
    @JsonAlias("STATE")
    private String state;
    @JsonAlias("POSTAL_CODE")
    private String postalCode;
    @JsonAlias("WEBSITE")
    private String website;
    @JsonAlias("PARTNER_PRIMARY_NEED_FROM_AWS")
    private String partnerPrimaryNeedFromAws;
    @JsonAlias("PARTNER_PROJECT_TITLE")
    private String partnerProjectTitle;
    @JsonAlias("CUSTOMER_BUSINESS_PROBLEM")
    private String customerBusinessProblem;
    @JsonAlias("SOLUTION_OFFERED")
    private String solutionOffered;
    @JsonAlias("OTHER_SOLUTION_OFFERED")
    private String otherSolutionOffered;
    @JsonAlias("NEXT_APPLICABLE_STEPS")
    private String nextApplicableSteps;
    @JsonAlias("USE_CASE")
    private String useCase;
    @JsonAlias("ESTIMATED_MONTHLY_REVENUE")
    private Double estimatedMonthlyRevenue;
    @JsonAlias("TARGET_CLOSE_DATE")
    private LocalDateTime targetCloseDate;
    @JsonAlias("OPPORTUNITY_TYPE")
    private String opportunityType;
    @JsonAlias("DELIVERY_MODEL")
    private String deliveryModel;
    @JsonAlias("IS_OPPORTUNITY_FROM_MARKETING")
    private Boolean isOpportunityFromMarketing;
    @JsonAlias("SALES_ACTIVITY")
    private String salesActivity;
    @JsonAlias("LINEITEM_USAGEACCOUNTID")
    private String lineitemUsageaccountid;
    @JsonAlias("PRIMARY_SALES_CONTACT_FIRST_NAME")
    private String primarySalesContactFirstName;
    @JsonAlias("PRIMARY_SALES_CONTACT_LAST_NAME")
    private String primarySalesContactLastName;
    @JsonAlias("PRIMARY_SALES_CONTACT_PHONE")
    private String primarySalesContactPhone;
    @JsonAlias("PRIMARY_SALES_CONTACT_EMAIL")
    private String primarySalesContactEmail;
    @JsonAlias("PRIMARY_SALES_CONTACT_DESIGNATION")
    private String primarySalesContactDesignation;
    @JsonAlias("OPPORTUNITY_RAISED")
    private Boolean opportunityRaised;
    @JsonAlias("OPPORTUNITY_RAISED_DATE")
    private LocalDateTime opportunityRaisedDate;
    @JsonAlias("OPPORTUNITY_RAISED_BY")
    private String opportunityRaisedBy;
    @JsonAlias("CLOUD_PLATFORM")
    private String cloudPlatform;
    @JsonAlias("PARTNER_NAME")
    private String partnerName;

}

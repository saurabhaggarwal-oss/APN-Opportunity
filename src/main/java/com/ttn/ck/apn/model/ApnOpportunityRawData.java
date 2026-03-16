package com.ttn.ck.apn.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Plain POJO representing a row in the Snowflake {@code apn_opportunity_raw_data}
 * table.
 * No JPA annotations — mapped manually from native query results.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApnOpportunityRawData {

  private String lineitemUuid;
  private String customerName;
  private String lineitemUsageaccountid;
  private String serviceName;
  private String mycloudRegionname;
  private String mycloudOperatingsystem;
  private String lineitemResourceid;
  private String mycloudInstancetype;
  private String productcode;
  private String finalNameTag;
  private String finalAutoscalingName;
  private String key;
  private String workloadTitle;
  private String workloadDescription;
  private BigDecimal totalPeriodCost;
  private Date resourceBirthDate;
  private Long activeDaysCount;
  private Long expectedDays;
  private Date loggedDate;
  private String cloudPlatform;

}

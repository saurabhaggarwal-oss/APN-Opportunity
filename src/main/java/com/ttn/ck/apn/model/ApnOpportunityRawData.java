package com.ttn.ck.apn.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

  @JsonAlias("RAW_UUID")
  private String lineitemUuid;
  @JsonAlias("CUSTOMER_NAME")
  private String customerName;
  @JsonAlias("LINEITEM_USAGEACCOUNTID")
  private String lineitemUsageaccountid;
  @JsonAlias("SERVICE_NAME")
  private String serviceName;
  @JsonAlias("MYCLOUD_REGIONNAME")
  private String mycloudRegionname;
  @JsonAlias("MYCLOUD_OPERATINGSYSTEM")
  private String mycloudOperatingsystem;
  @JsonAlias("LINEITEM_RESOURCEID")
  private String lineitemResourceid;
  @JsonAlias("MYCLOUD_INSTANCETYPE")
  private String mycloudInstancetype;
  @JsonAlias("PRODUCTCODE")
  private String productcode;
  @JsonAlias("FINAL_NAME_TAG")
  private String finalNameTag;
  @JsonAlias("FINAL_AUTOSCALING_NAME")
  private String finalAutoscalingName;
  @JsonAlias("KEY")
  private String key;
  @JsonAlias("WORKLOAD_TITLE")
  private String workloadTitle;
  @JsonAlias("WORKLOAD_DESCRIPTION")
  private String workloadDescription;
  @JsonAlias("TOTAL_PERIOD_COST")
  private BigDecimal totalPeriodCost;
  @JsonAlias("RESOURCE_BIRTH_DATE")
  private LocalDateTime resourceBirthDate;
  @JsonAlias("ACTIVE_DAYS_COUNT")
  private Long activeDaysCount;
  @JsonAlias("EXPECTED_DAYS")
  private Long expectedDays;
  @JsonAlias("LOGGED_DATE")
  private LocalDateTime loggedDate;
  @JsonAlias("CLOUD_PLATFORM")
  private String cloudPlatform;

}

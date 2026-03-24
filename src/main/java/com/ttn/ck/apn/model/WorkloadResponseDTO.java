package com.ttn.ck.apn.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for parsing the ChatGPT workload generation response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadResponseDTO {

    @JsonAlias("LINEITEM_UUID")
    private String lineitemUuid;

    @JsonAlias("CUSTOMER_NAME")
    private String customerName;

    @JsonAlias("LINEITEM_USAGEACCOUNTID")
    private String accountId;

    @JsonAlias("Workload Title")
    private String workloadTitle;

    @JsonAlias("Workload Description")
    private String workloadDescription;

}

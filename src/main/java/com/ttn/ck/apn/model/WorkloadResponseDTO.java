package com.ttn.ck.apn.model;

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
    private String lineitemUuid;
    private String workloadTitle;
    private String workloadDescription;
}

package com.ttn.ck.apn.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event model representing a processed workload ready for RabbitMQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadEvent {

    private String customerName;
    private String partnerName;
    private String accountId;
    private String workloadDescription;

}

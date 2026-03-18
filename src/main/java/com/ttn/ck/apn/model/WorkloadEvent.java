package com.ttn.ck.apn.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Event model representing a processed workload ready for RabbitMQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadEvent implements Serializable {
    private String customerName;
    private String partnerName;
}

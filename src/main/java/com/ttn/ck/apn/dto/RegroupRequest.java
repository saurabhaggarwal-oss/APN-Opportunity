package com.ttn.ck.apn.dto;

import lombok.Data;

import java.util.List;

@Data
public class RegroupRequest {
    /**
     * Allowed values: CUSTOMER, PRODUCT, REGION, NAME_TAG, AUTOSCALING_NAME,
     *                  INSTANCE_TYPE, OPERATING_SYSTEM, ACCOUNT_ID, RESOURCE_ARN
     */
    private List<String> groupingFields;

    /** Optional: only regroup resources belonging to this customer */
    private String customerName;
}

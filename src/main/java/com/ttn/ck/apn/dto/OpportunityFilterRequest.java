package com.ttn.ck.apn.dto;

import lombok.Data;

@Data
public class OpportunityFilterRequest {
    private String customerName;
    private String productName;
    private String region;
    private String status;
    private String search;
}

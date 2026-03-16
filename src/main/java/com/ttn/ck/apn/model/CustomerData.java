package com.ttn.ck.apn.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerData {

    private Date createdDate;
    private Date modifiedDate;
    private String customerName;
    private String customerCompanyName;
    private String industry;
    private String industryOther;
    private String country;
    private String state;
    private String postalCode;
    private String website;
    private String partnerName;

}

package com.ttn.ck.apn.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerData {

    @JsonAlias("CUSTOMER_UUID")
    private String uuid;
    @JsonAlias("CREATED_DATE")
    private LocalDateTime createdDate;
    @JsonAlias("MODIFIED_DATE")
    private LocalDateTime modifiedDate;
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
    @JsonAlias("PARTNER_NAME")
    private String partnerName;

}

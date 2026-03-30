package com.ttn.ck.apn.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_mapping")
public class CustomerMapping {

    @Id
    @Column(name = "billdesk_customer_name", nullable = false, updatable = false)
    private String billdeskCustomerName;

    @Column(name = "apn_legal_name")
    private String apnLegalName;

    @Column(name = "industry")
    private String industry;

    @Column(name = "country")
    private String country;

    @Column(name = "state_province")
    private String stateProvince;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "website")
    private String website;
}

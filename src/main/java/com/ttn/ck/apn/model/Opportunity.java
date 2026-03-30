package com.ttn.ck.apn.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "opportunity")
public class Opportunity {

    @Id
    @Column(name = "group_key", nullable = false, updatable = false)
    private String groupKey;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "apn_legal_name")
    private String apnLegalName;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "region")
    private String region;

    @Column(name = "name_tag")
    private String nameTag;

    @Column(name = "instance_types", columnDefinition = "TEXT")
    private String instanceTypes;

    @Column(name = "resource_count")
    private Integer resourceCount;

    @Column(name = "estimated_mrr", precision = 18, scale = 4)
    private BigDecimal estimatedMrr;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "grouping_reason", columnDefinition = "TEXT")
    private String groupingReason;

    @Column(name = "grouping_fields")
    private String groupingFields;

    @Column(name = "use_case")
    private String useCase;

    @Column(name = "opportunity_type")
    private String opportunityType; // Expansion / Net New Business / Flat Renewal

    @Column(name = "status")
    private String status; // draft -> reviewed -> exported
}

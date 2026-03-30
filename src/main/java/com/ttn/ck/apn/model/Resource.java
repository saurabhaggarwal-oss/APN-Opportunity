package com.ttn.ck.apn.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resource")
public class Resource {

    @Id
    @Column(name = "lineitem_resource_id", nullable = false, updatable = false)
    private String lineitemResourceId; // Unique AWS ARN/ID - Dedup key

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "region")
    private String region;

    @Column(name = "operating_system")
    private String operatingSystem;

    @Column(name = "instance_type")
    private String instanceType;

    @Column(name = "final_name_tag")
    private String finalNameTag;

    @Column(name = "final_autoscaling_name")
    private String finalAutoscalingName;

    @Column(name = "resource_birth_date")
    private LocalDate resourceBirthDate;

    @Column(name = "total_period_cost", precision = 18, scale = 4)
    private BigDecimal totalPeriodCost;

    @Column(name = "active_days_count")
    private Double activeDaysCount;

    @Column(name = "persistence_pct")
    private Double persistencePct;

    @Column(name = "group_key")
    private String groupKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", referencedColumnName = "id")
    private UploadBatch batch;
}

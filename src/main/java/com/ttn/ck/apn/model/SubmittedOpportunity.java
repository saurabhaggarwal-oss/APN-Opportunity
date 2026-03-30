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
@Table(name = "submitted_opportunity")
public class SubmittedOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "year_val")
    private Integer year;

    @Column(name = "month_val")
    private Integer month;

    @Column(name = "opportunity_count")
    private Integer opportunityCount;

    @Column(name = "mrr", precision = 18, scale = 4)
    private BigDecimal mrr;
}

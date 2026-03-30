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
@Table(name = "resource_snapshot")
public class ResourceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lineitem_resource_id", nullable = false)
    private String lineitemResourceId;

    @Column(name = "snapshot_data", columnDefinition = "JSON")
    private String snapshotData; // JSON representation of the resource before change

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", referencedColumnName = "id")
    private UploadBatch batch;
}

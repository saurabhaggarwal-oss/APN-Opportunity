package com.ttn.ck.apn.repository;

import com.ttn.ck.apn.model.ResourceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceSnapshotRepository extends JpaRepository<ResourceSnapshot, Long> {
    List<ResourceSnapshot> findByBatchId(Long batchId);
}

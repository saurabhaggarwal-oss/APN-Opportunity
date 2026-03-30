package com.ttn.ck.apn.repository;

import com.ttn.ck.apn.model.UploadBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UploadBatchRepository extends JpaRepository<UploadBatch, Long> {

    List<UploadBatch> findAllByOrderByUploadDateDesc();
}

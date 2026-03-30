package com.ttn.ck.apn.repository;

import com.ttn.ck.apn.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {

    List<Resource> findByGroupKey(String groupKey);

    List<Resource> findByCustomerName(String customerName);

    List<Resource> findByCustomerNameContainingIgnoreCase(String customerName);

    @Query("SELECT r FROM Resource r WHERE " +
            "(:customerName IS NULL OR LOWER(r.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
            "(:productName IS NULL OR LOWER(r.productName) LIKE LOWER(CONCAT('%', :productName, '%'))) AND " +
            "(:region IS NULL OR LOWER(r.region) LIKE LOWER(CONCAT('%', :region, '%'))) AND " +
            "(:search IS NULL OR LOWER(r.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(r.productName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(r.lineitemResourceId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(r.finalNameTag) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Resource> findWithFilters(
            @Param("customerName") String customerName,
            @Param("productName") String productName,
            @Param("region") String region,
            @Param("search") String search
    );

    @Query("SELECT DISTINCT r.customerName FROM Resource r WHERE r.customerName IS NOT NULL ORDER BY r.customerName")
    List<String> findDistinctCustomerNames();

    @Query("SELECT DISTINCT r.productName FROM Resource r WHERE r.productName IS NOT NULL ORDER BY r.productName")
    List<String> findDistinctProductNames();

    @Query("SELECT DISTINCT r.region FROM Resource r WHERE r.region IS NOT NULL ORDER BY r.region")
    List<String> findDistinctRegions();
}

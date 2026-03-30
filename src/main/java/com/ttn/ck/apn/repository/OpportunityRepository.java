package com.ttn.ck.apn.repository;

import com.ttn.ck.apn.model.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, String> {

    List<Opportunity> findByStatus(String status);

    List<Opportunity> findByCustomerName(String customerName);

    List<Opportunity> findByCustomerNameContainingIgnoreCase(String customerName);

    List<Opportunity> findByProductName(String productName);

    List<Opportunity> findByRegion(String region);

    @Query("SELECT o FROM Opportunity o WHERE " +
            "(:customerName IS NULL OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
            "(:productName IS NULL OR LOWER(o.productName) LIKE LOWER(CONCAT('%', :productName, '%'))) AND " +
            "(:region IS NULL OR LOWER(o.region) LIKE LOWER(CONCAT('%', :region, '%'))) AND " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:search IS NULL OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(o.productName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(o.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(o.nameTag) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Opportunity> findWithFilters(
            @Param("customerName") String customerName,
            @Param("productName") String productName,
            @Param("region") String region,
            @Param("status") String status,
            @Param("search") String search
    );

    @Query("SELECT DISTINCT o.customerName FROM Opportunity o WHERE o.customerName IS NOT NULL ORDER BY o.customerName")
    List<String> findDistinctCustomerNames();

    @Query("SELECT DISTINCT o.productName FROM Opportunity o WHERE o.productName IS NOT NULL ORDER BY o.productName")
    List<String> findDistinctProductNames();

    @Query("SELECT DISTINCT o.region FROM Opportunity o WHERE o.region IS NOT NULL ORDER BY o.region")
    List<String> findDistinctRegions();
}

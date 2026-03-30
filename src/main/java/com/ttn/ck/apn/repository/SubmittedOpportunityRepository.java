package com.ttn.ck.apn.repository;

import com.ttn.ck.apn.model.SubmittedOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SubmittedOpportunityRepository extends JpaRepository<SubmittedOpportunity, Long> {

    List<SubmittedOpportunity> findByCustomerName(String customerName);

    List<SubmittedOpportunity> findByYear(Integer year);

    @Query("SELECT s FROM SubmittedOpportunity s WHERE " +
            "(:customerName IS NULL OR LOWER(s.customerName) LIKE LOWER(CONCAT('%', :customerName, '%')))")
    List<SubmittedOpportunity> findWithFilters(@Param("customerName") String customerName);

    @Query("SELECT s.year, s.month, SUM(s.opportunityCount), SUM(s.mrr) FROM SubmittedOpportunity s " +
            "GROUP BY s.year, s.month ORDER BY s.year ASC, s.month ASC")
    List<Object[]> getMonthlyTrends();

    @Query("SELECT s.customerName, SUM(s.opportunityCount), SUM(s.mrr) FROM SubmittedOpportunity s " +
            "GROUP BY s.customerName ORDER BY SUM(s.mrr) DESC")
    List<Object[]> getTopCustomersByMrr();

    @Query("SELECT s.year, s.month, SUM(s.opportunityCount), SUM(s.mrr), COUNT(DISTINCT s.customerName) " +
            "FROM SubmittedOpportunity s GROUP BY s.year, s.month ORDER BY s.year ASC, s.month ASC")
    List<Object[]> getMonthlyBreakdown();

    @Query("SELECT s.year, s.month, SUM(s.opportunityCount), SUM(s.mrr) FROM SubmittedOpportunity s " +
            "WHERE s.customerName = :customerName GROUP BY s.year, s.month ORDER BY s.year ASC, s.month ASC")
    List<Object[]> getMonthlyTrendsByCustomer(@Param("customerName") String customerName);

    @Query("SELECT COUNT(DISTINCT s.customerName) FROM SubmittedOpportunity s")
    long countDistinctCustomers();

    @Query("SELECT SUM(s.opportunityCount) FROM SubmittedOpportunity s")
    Long sumTotalOpportunities();

    @Query("SELECT SUM(s.mrr) FROM SubmittedOpportunity s")
    BigDecimal sumTotalMrr();

    @Query("SELECT s.customerName, SUM(s.opportunityCount), SUM(s.mrr), MAX(s.year), MAX(s.month) " +
            "FROM SubmittedOpportunity s GROUP BY s.customerName ORDER BY s.customerName")
    List<Object[]> getCustomerSummaries();
}

package com.ttn.ck.apn.dao;

import com.ttn.ck.apn.model.ApnOpportunityMasterData;
import com.ttn.ck.apn.model.ApnOpportunityRawData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * JDBC-based implementation of {@link ApnOpportunityDataDao}.
 * Uses native Snowflake SQL queries via Spring's {@link JdbcTemplate}.
 *
 * <p>All queries target the Snowflake schema configured in application.yaml.
 * Row mapping is done manually since we use plain POJOs (no JPA entities).</p>
 */
@Repository
public class ApnOpportunityDataDaoImpl implements ApnOpportunityDataDao {

    private static final Logger log = LoggerFactory.getLogger(ApnOpportunityDataDaoImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public ApnOpportunityDataDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Row Mappers
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Maps a ResultSet row to an {@link ApnOpportunityMasterData} POJO.
     */
    private static final RowMapper<ApnOpportunityMasterData> MASTER_DATA_ROW_MAPPER =
            (ResultSet rs, int rowNum) -> ApnOpportunityMasterData.builder()
                    .createdDate(rs.getTimestamp("CREATED_DATE"))
                    .modifiedDate(rs.getTimestamp("MODIFIED_DATE"))
                    .lineitemUuid(rs.getString("LINEITEM_UUID"))
                    .customerName(rs.getString("CUSTOMER_NAME"))
                    .customerCompanyName(rs.getString("CUSTOMER_COMPANY_NAME"))
                    .industry(rs.getString("INDUSTRY"))
                    .industryOther(rs.getString("INDUSTRY_OTHER"))
                    .country(rs.getString("COUNTRY"))
                    .state(rs.getString("STATE"))
                    .postalCode(rs.getString("POSTAL_CODE"))
                    .website(rs.getString("WEBSITE"))
                    .partnerPrimaryNeedFromAws(rs.getString("PARTNER_PRIMARY_NEED_FROM_AWS"))
                    .partnerProjectTitle(rs.getString("PARTNER_PROJECT_TITLE"))
                    .customerBusinessProblem(rs.getString("CUSTOMER_BUSINESS_PROBLEM"))
                    .solutionOffered(rs.getString("SOLUTION_OFFERED"))
                    .otherSolutionOffered(rs.getString("OTHER_SOLUTION_OFFERED"))
                    .nextApplicableSteps(rs.getString("NEXT_APPLICABLE_STEPS"))
                    .useCase(rs.getString("USE_CASE"))
                    .estimatedMonthlyRevenue(getDoubleOrNull(rs, "ESTIMATED_MONTHLY_REVENUE"))
                    .targetCloseDate(rs.getTimestamp("TARGET_CLOSE_DATE"))
                    .opportunityType(rs.getString("OPPORTUNITY_TYPE"))
                    .deliveryModel(rs.getString("DELIVERY_MODEL"))
                    .isOpportunityFromMarketing(getBooleanOrNull(rs, "IS_OPPORTUNITY_FROM_MARKETING"))
                    .salesActivity(rs.getString("SALES_ACTIVITY"))
                    .lineitemUsageaccountid(rs.getString("LINEITEM_USAGEACCOUNTID"))
                    .primarySalesContactFirstName(rs.getString("PRIMARY_SALES_CONTACT_FIRST_NAME"))
                    .primarySalesContactLastName(rs.getString("PRIMARY_SALES_CONTACT_LAST_NAME"))
                    .primarySalesContactPhone(rs.getString("PRIMARY_SALES_CONTACT_PHONE"))
                    .primarySalesContactEmail(rs.getString("PRIMARY_SALES_CONTACT_EMAIL"))
                    .primarySalesContactDesignation(rs.getString("PRIMARY_SALES_CONTACT_DESIGNATION"))
                    .opportunityRaised(getBooleanOrNull(rs, "OPPORTUNITY_RAISED"))
                    .opportunityRaisedDate(rs.getTimestamp("OPPORTUNITY_RAISED_DATE"))
                    .opportunityRaisedBy(rs.getString("OPPORTUNITY_RAISED_BY"))
                    .loggedDate(rs.getTimestamp("LOGGED_DATE"))
                    .cloudPlatform(rs.getString("CLOUD_PLATFORM"))
                    .partnerName(rs.getString("PARTNER_NAME"))
                    .build();

    /**
     * Maps a ResultSet row to an {@link ApnOpportunityRawData} POJO.
     */
    private static final RowMapper<ApnOpportunityRawData> RAW_DATA_ROW_MAPPER =
            (ResultSet rs, int rowNum) -> ApnOpportunityRawData.builder()
                    .lineitemUuid(rs.getString("LINEITEM_UUID"))
                    .customerName(rs.getString("CUSTOMER_NAME"))
                    .lineitemUsageaccountid(rs.getString("LINEITEM_USAGEACCOUNTID"))
                    .serviceName(rs.getString("SERVICE_NAME"))
                    .mycloudRegionname(rs.getString("MYCLOUD_REGIONNAME"))
                    .mycloudOperatingsystem(rs.getString("MYCLOUD_OPERATINGSYSTEM"))
                    .lineitemResourceid(rs.getString("LINEITEM_RESOURCEID"))
                    .mycloudInstancetype(rs.getString("MYCLOUD_INSTANCETYPE"))
                    .productcode(rs.getString("PRODUCTCODE"))
                    .finalNameTag(rs.getString("FINAL_NAME_TAG"))
                    .finalAutoscalingName(rs.getString("FINAL_AUTOSCALING_NAME"))
                    .key(rs.getString("KEY"))
                    .workloadTitle(rs.getString("WORKLOAD_TITLE"))
                    .workloadDescription(rs.getString("WORKLOAD_DESCRIPTION"))
                    .totalPeriodCost(rs.getBigDecimal("TOTAL_PERIOD_COST"))
                    .resourceBirthDate(rs.getTimestamp("RESOURCE_BIRTH_DATE"))
                    .activeDaysCount(getLongOrNull(rs, "ACTIVE_DAYS_COUNT"))
                    .expectedDays(getLongOrNull(rs, "EXPECTED_DAYS"))
                    .loggedDate(rs.getTimestamp("LOGGED_DATE"))
                    .cloudPlatform(rs.getString("CLOUD_PLATFORM"))
                    .build();

    // ═══════════════════════════════════════════════════════════════════════
    //  Master Data Operations
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public List<ApnOpportunityMasterData> findMasterDataByFilters(
            Date startDate, Date endDate, Boolean opportunityRaised) {

        log.debug("Querying master data: startDate={}, endDate={}, raised={}",
                startDate, endDate, opportunityRaised);

        String sql = """
                SELECT *
                FROM apn_opportunity_master_data
                WHERE OPPORTUNITY_RAISED = ?
                  AND OPPORTUNITY_RAISED_DATE BETWEEN ? AND ?
                ORDER BY MODIFIED_DATE DESC
                """;

        return jdbcTemplate.query(sql, MASTER_DATA_ROW_MAPPER,
                opportunityRaised, startDate, endDate);
    }

    @Override
    public List<ApnOpportunityMasterData> findMasterDataByUuids(List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return Collections.emptyList();
        }

        log.debug("Fetching master data for {} UUIDs", uuids.size());

        // Build parameterized IN clause
        String placeholders = String.join(",", Collections.nCopies(uuids.size(), "?"));
        String sql = String.format(
                "SELECT * FROM apn_opportunity_master_data WHERE LINEITEM_UUID IN (%s)",
                placeholders);

        return jdbcTemplate.query(sql, MASTER_DATA_ROW_MAPPER, uuids.toArray());
    }

    @Override
    public int updateOpportunityRaised(String uuid, Boolean raised, Date raisedDate, String raisedBy) {
        log.info("Updating opportunity raised status: uuid={}, raised={}, by={}", uuid, raised, raisedBy);

        String sql = """
                UPDATE apn_opportunity_master_data
                SET OPPORTUNITY_RAISED = ?,
                    OPPORTUNITY_RAISED_DATE = ?,
                    OPPORTUNITY_RAISED_BY = ?,
                    MODIFIED_DATE = CURRENT_TIMESTAMP()
                WHERE LINEITEM_UUID = ?
                """;

        return jdbcTemplate.update(sql, raised, raisedDate, raisedBy, uuid);
    }

    @Override
    public int upsertMasterData(ApnOpportunityMasterData data) {
        log.debug("Upserting master data for UUID: {}", data.getLineitemUuid());

        // Snowflake MERGE statement for upsert
        String sql = """
                MERGE INTO apn_opportunity_master_data AS target
                USING (SELECT ? AS LINEITEM_UUID) AS source
                ON target.LINEITEM_UUID = source.LINEITEM_UUID
                WHEN MATCHED THEN
                    UPDATE SET
                        MODIFIED_DATE = CURRENT_TIMESTAMP(),
                        CUSTOMER_NAME = ?,
                        CUSTOMER_COMPANY_NAME = ?,
                        INDUSTRY = ?,
                        INDUSTRY_OTHER = ?,
                        COUNTRY = ?,
                        STATE = ?,
                        POSTAL_CODE = ?,
                        WEBSITE = ?,
                        PARTNER_PRIMARY_NEED_FROM_AWS = ?,
                        PARTNER_PROJECT_TITLE = ?,
                        CUSTOMER_BUSINESS_PROBLEM = ?,
                        SOLUTION_OFFERED = ?,
                        OTHER_SOLUTION_OFFERED = ?,
                        NEXT_APPLICABLE_STEPS = ?,
                        USE_CASE = ?,
                        ESTIMATED_MONTHLY_REVENUE = ?,
                        TARGET_CLOSE_DATE = ?,
                        OPPORTUNITY_TYPE = ?,
                        DELIVERY_MODEL = ?,
                        IS_OPPORTUNITY_FROM_MARKETING = ?,
                        SALES_ACTIVITY = ?,
                        LINEITEM_USAGEACCOUNTID = ?,
                        PRIMARY_SALES_CONTACT_FIRST_NAME = ?,
                        PRIMARY_SALES_CONTACT_LAST_NAME = ?,
                        PRIMARY_SALES_CONTACT_PHONE = ?,
                        PRIMARY_SALES_CONTACT_EMAIL = ?,
                        PRIMARY_SALES_CONTACT_DESIGNATION = ?,
                        LOGGED_DATE = ?,
                        CLOUD_PLATFORM = ?,
                        PARTNER_NAME = ?
                WHEN NOT MATCHED THEN
                    INSERT (
                        CREATED_DATE, MODIFIED_DATE, LINEITEM_UUID,
                        CUSTOMER_NAME, CUSTOMER_COMPANY_NAME,
                        INDUSTRY, INDUSTRY_OTHER, COUNTRY, STATE, POSTAL_CODE,
                        WEBSITE, PARTNER_PRIMARY_NEED_FROM_AWS, PARTNER_PROJECT_TITLE,
                        CUSTOMER_BUSINESS_PROBLEM, SOLUTION_OFFERED, OTHER_SOLUTION_OFFERED,
                        NEXT_APPLICABLE_STEPS, USE_CASE, ESTIMATED_MONTHLY_REVENUE,
                        TARGET_CLOSE_DATE, OPPORTUNITY_TYPE, DELIVERY_MODEL,
                        IS_OPPORTUNITY_FROM_MARKETING, SALES_ACTIVITY,
                        LINEITEM_USAGEACCOUNTID,
                        PRIMARY_SALES_CONTACT_FIRST_NAME, PRIMARY_SALES_CONTACT_LAST_NAME,
                        PRIMARY_SALES_CONTACT_PHONE, PRIMARY_SALES_CONTACT_EMAIL,
                        PRIMARY_SALES_CONTACT_DESIGNATION,
                        OPPORTUNITY_RAISED, OPPORTUNITY_RAISED_DATE, OPPORTUNITY_RAISED_BY,
                        LOGGED_DATE, CLOUD_PLATFORM, PARTNER_NAME
                    )
                    VALUES (
                        CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), ?,
                        ?, ?,
                        ?, ?, ?, ?, ?,
                        ?, ?, ?,
                        ?, ?, ?,
                        ?, ?, ?,
                        ?, ?, ?,
                        ?, ?,
                        ?,
                        ?, ?,
                        ?, ?,
                        ?,
                        FALSE, NULL, NULL,
                        ?, ?, ?
                    )
                """;

        return jdbcTemplate.update(sql,
                // USING clause
                data.getLineitemUuid(),
                // WHEN MATCHED – UPDATE SET values
                data.getCustomerName(),
                data.getCustomerCompanyName(),
                data.getIndustry(),
                data.getIndustryOther(),
                data.getCountry(),
                data.getState(),
                data.getPostalCode(),
                data.getWebsite(),
                data.getPartnerPrimaryNeedFromAws(),
                data.getPartnerProjectTitle(),
                data.getCustomerBusinessProblem(),
                data.getSolutionOffered(),
                data.getOtherSolutionOffered(),
                data.getNextApplicableSteps(),
                data.getUseCase(),
                data.getEstimatedMonthlyRevenue(),
                data.getTargetCloseDate(),
                data.getOpportunityType(),
                data.getDeliveryModel(),
                data.getIsOpportunityFromMarketing(),
                data.getSalesActivity(),
                data.getLineitemUsageaccountid(),
                data.getPrimarySalesContactFirstName(),
                data.getPrimarySalesContactLastName(),
                data.getPrimarySalesContactPhone(),
                data.getPrimarySalesContactEmail(),
                data.getPrimarySalesContactDesignation(),
                data.getLoggedDate(),
                data.getCloudPlatform(),
                data.getPartnerName(),
                // WHEN NOT MATCHED – INSERT VALUES
                data.getLineitemUuid(),
                data.getCustomerName(),
                data.getCustomerCompanyName(),
                data.getIndustry(),
                data.getIndustryOther(),
                data.getCountry(),
                data.getState(),
                data.getPostalCode(),
                data.getWebsite(),
                data.getPartnerPrimaryNeedFromAws(),
                data.getPartnerProjectTitle(),
                data.getCustomerBusinessProblem(),
                data.getSolutionOffered(),
                data.getOtherSolutionOffered(),
                data.getNextApplicableSteps(),
                data.getUseCase(),
                data.getEstimatedMonthlyRevenue(),
                data.getTargetCloseDate(),
                data.getOpportunityType(),
                data.getDeliveryModel(),
                data.getIsOpportunityFromMarketing(),
                data.getSalesActivity(),
                data.getLineitemUsageaccountid(),
                data.getPrimarySalesContactFirstName(),
                data.getPrimarySalesContactLastName(),
                data.getPrimarySalesContactPhone(),
                data.getPrimarySalesContactEmail(),
                data.getPrimarySalesContactDesignation(),
                data.getLoggedDate(),
                data.getCloudPlatform(),
                data.getPartnerName()
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Raw Data Operations
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public List<ApnOpportunityRawData> findRawDataByUuid(String uuid) {
        log.debug("Fetching raw data for UUID: {}", uuid);

        String sql = """
                SELECT *
                FROM apn_opportunity_raw_data
                WHERE LINEITEM_UUID = ?
                ORDER BY LOGGED_DATE DESC
                """;

        return jdbcTemplate.query(sql, RAW_DATA_ROW_MAPPER, uuid);
    }

    @Override
    public List<ApnOpportunityRawData> findAllRawData() {
        log.debug("Fetching all raw data records for refresh processing");

        String sql = "SELECT * FROM apn_opportunity_raw_data ORDER BY LOGGED_DATE DESC";

        return jdbcTemplate.query(sql, RAW_DATA_ROW_MAPPER);
    }

    @Override
    public List<String> findDistinctRawDataUuids() {
        log.debug("Fetching distinct UUIDs from raw data");

        String sql = "SELECT DISTINCT LINEITEM_UUID FROM apn_opportunity_raw_data";

        return jdbcTemplate.queryForList(sql, String.class);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Utility Methods
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Safely reads a nullable Double from the ResultSet.
     */
    private static Double getDoubleOrNull(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    /**
     * Safely reads a nullable Boolean from the ResultSet.
     */
    private static Boolean getBooleanOrNull(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        return rs.wasNull() ? null : value;
    }

    /**
     * Safely reads a nullable Long from the ResultSet.
     */
    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}

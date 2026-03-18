package com.ttn.ck.apn.dao.impl;

import com.ttn.ck.apn.dao.ApnOpportunityDataDao;
import com.ttn.ck.apn.model.ApnOpportunityMasterData;
import com.ttn.ck.apn.model.ApnOpportunityRawData;
import com.ttn.ck.apn.queries.ApnOpportunityQueries;
import com.ttn.ck.apn.utils.ApnUtils;
import com.ttn.ck.authX.service.AuthorizedUserService;
import com.ttn.ck.queryprocessor.service.PersistenceQueryExecutor;
import com.ttn.ck.queryprocessor.service.QueryExecutor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * JDBC-based implementation of {@link ApnOpportunityDataDao}.
 * Uses native Snowflake SQL queries via Spring's
 *
 * <p>All queries target the Snowflake schema configured in application.yaml.
 * Row mapping is done manually since we use plain POJOs (no JPA entities).</p>
 */

@Slf4j
@Repository
@AllArgsConstructor
public class ApnOpportunityDataDaoImpl implements ApnOpportunityDataDao {

    private final ApnOpportunityQueries opportunityQueries;
    private final AuthorizedUserService authorizedUserService;

    @Override
    public List<ApnOpportunityMasterData> findMasterDataByFilters(String startDate, String endDate, Boolean opportunityRaised) {
        log.debug("Querying master data: startDate={}, endDate={}, raised={}", startDate, endDate, opportunityRaised);
        Map<String, Object> params = ApnUtils.defaultApnParams(startDate, endDate);
        params.put("opportunityRaised", opportunityRaised);
        return QueryExecutor.getDynamicResult(opportunityQueries.getOpportunityMasterData(), params, ApnOpportunityMasterData.class);
    }

    @Override
    public List<ApnOpportunityMasterData> findMasterDataByUuids(List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return Collections.emptyList();
        }
        log.debug("Fetching master data for {} UUIDs", uuids.size());
        Map<String, Object> params = new HashMap<>();
        params.put("opportunityMasterUuids", uuids);
        return QueryExecutor.getDynamicResult(opportunityQueries.getOpportunityMasterDataByUuid(), params, ApnOpportunityMasterData.class);
    }

    @Override
    public void updateOpportunityRaised(Set<String> uuid, Boolean raised) {
        log.info("Updating opportunity raised status: uuid={}, raised={}", uuid, raised);
        Map<String, Object> param = new HashMap<>();
        param.put("user", Boolean.TRUE.equals(raised) ? authorizedUserService.getEmail() : Strings.EMPTY);
        param.put("opportunityRaised", raised);
        param.put("masterUuid", uuid);
        int records = PersistenceQueryExecutor.save(opportunityQueries.getUpdateOpportunityRaiseStatus(), param);
        log.info("{} opportunity raised status updated", records);
    }

    @Override
    public List<ApnOpportunityRawData> findRawDataByUuid(String uuid) {
        log.debug("Fetching raw data for UUID: {}", uuid);
        Map<String, Object> param = new HashMap<>();
        param.put("masterUuid", uuid);
        return QueryExecutor.getDynamicResult(opportunityQueries.getOpportunityRawDataByMasterUuid(), param, ApnOpportunityRawData.class);
    }

    @Override
    public List<ApnOpportunityRawData> fetchUnprocessedRawData() {
        log.debug("Fetching unprocessed raw data");
        return QueryExecutor.getDynamicResult(opportunityQueries.getOpportunityUnprocessedRawData(), new HashMap<>(), ApnOpportunityRawData.class);
    }

    @Override
    public void updateWorkloadDetailsByLineItemUuid(String lineitemUuid, String workloadTitle, String workloadDescription) {
        log.info("Updating workload details for lineitem UUID: {}", lineitemUuid);
        Map<String, Object> param = new HashMap<>();
        param.put("lineitemUuid", lineitemUuid);
        param.put("workloadTitle", workloadTitle);
        param.put("workloadDescription", workloadDescription);
        int records = PersistenceQueryExecutor.save(opportunityQueries.getUpdateOpportunityRawDataByLineitemUuid(), param);
        log.debug("{} opportunity raw data workload details updated", records);
    }

    @Override
    public void insertOpportunityMasterData(String customerName, String partnerName, String workloadDescription) {
        log.info("Inserting opportunity master data for customer: {}", customerName);
        Map<String, Object> param = new HashMap<>();
        param.put("customerName", customerName);
        param.put("workloadDescription", workloadDescription);
        int records = PersistenceQueryExecutor.save(opportunityQueries.getInsertOpportunityMasterData(), param);
        log.debug("{} opportunity master data inserted", records);
    }

    @Override
    public void insertOpportunityMappingData(String customerName, String partnerName, String workloadDescription) {
        log.info("Inserting opportunity mapping data for customer: {}", customerName);
        Map<String, Object> param = new HashMap<>();
        param.put("customerName", customerName);
        param.put("accountId", partnerName);
        param.put("workloadDescription", workloadDescription);
        int records = PersistenceQueryExecutor.save(opportunityQueries.getInsertOpportunityMappingData(), param);
        log.debug("{} opportunity mapping data inserted", records);
    }

}

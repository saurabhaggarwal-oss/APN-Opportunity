package com.ttn.ck.apn.dao.impl;

import com.ttn.ck.apn.dao.CustomerCommunicationDataDao;
import com.ttn.ck.apn.model.CustomerData;
import com.ttn.ck.apn.queries.CustomerCommunicationQueries;
import com.ttn.ck.queryprocessor.service.PersistenceQueryExecutor;
import com.ttn.ck.queryprocessor.service.QueryExecutor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC-based implementation of {@link CustomerCommunicationDataDao}.
 * Uses native Snowflake SQL queries via QueryExecutor/PersistenceQueryExecutor.
 *
 * <p>All queries target the Snowflake {@code customer_communication_data} table.
 * Row mapping is done automatically to the {@link CustomerData} POJO.</p>
 */
@Slf4j
@Repository
@AllArgsConstructor
public class CustomerCommunicationDataDaoImpl implements CustomerCommunicationDataDao {

    private final CustomerCommunicationQueries queries;

    @Override
    public List<CustomerData> findAll(String partnerName) {
        log.debug("Querying all customer communication data for partner: {}", partnerName);
        Map<String, Object> params = new HashMap<>();
        params.put("partnerName", partnerName);
        return QueryExecutor.getDynamicResult(queries.getGetAllCustomers(), params, CustomerData.class);
    }

    @Override
    public int addCustomer(CustomerData data) {
        log.debug("Inserting new customer communication record: {}", data.getCustomerName());
        Map<String, Object> params = buildCustomerParams(data);
        int rows = PersistenceQueryExecutor.save(queries.getAddCustomer(), params);
        log.info("Inserted {} customer communication record(s)", rows);
        return rows;
    }

    @Override
    public int updateCustomer(CustomerData data) {
        log.debug("Updating customer communication record ID: {}", data.getUuid());
        Map<String, Object> params = buildCustomerParams(data);
        params.put("uuid", data.getUuid());
        int rows = PersistenceQueryExecutor.save(queries.getUpdateCustomer(), params);
        log.info("Updated {} customer communication record(s)", rows);
        return rows;
    }

    @Override
    public int deleteCustomer(String uuid) {
        log.debug("Deleting customer communication record: {}", uuid);
        Map<String, Object> params = new HashMap<>();
        params.put("uuid", uuid);
        int rows = PersistenceQueryExecutor.save(queries.getDeleteCustomer(), params);
        log.info("Deleted {} customer communication record(s)", rows);
        return rows;
    }

    @Override
    public CustomerData findCustomerByUuid(String uuid) {
        log.debug("Fetching customer communication record: {}", uuid);
        Map<String, Object> params = new HashMap<>();
        params.put("uuid", uuid);
        List<CustomerData> results = QueryExecutor.getDynamicResult(queries.getByUuid(), params, CustomerData.class);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Builds common parameter map from a CustomerData object.
     */
    private Map<String, Object> buildCustomerParams(CustomerData data) {
        Map<String, Object> params = new HashMap<>();
        params.put("customerName", data.getCustomerName());
        params.put("customerCompanyName", data.getCustomerCompanyName());
        params.put("industry", data.getIndustry());
        params.put("industryOther", data.getIndustryOther());
        params.put("country", data.getCountry());
        params.put("state", data.getState());
        params.put("postalCode", data.getPostalCode());
        params.put("website", data.getWebsite());
        params.put("partnerName", data.getPartnerName());
        return params;
    }
}

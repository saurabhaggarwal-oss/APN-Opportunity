package com.ttn.ck.apn.dao.impl;

import com.ttn.ck.apn.dao.CustomerCommunicationDataDao;
import com.ttn.ck.apn.model.CustomerData;
import com.ttn.ck.apn.queries.CustomerCommunicationQueries;
import com.ttn.ck.queryprocessor.service.PersistenceQueryExecutor;
import com.ttn.ck.queryprocessor.service.QueryExecutor;
import jakarta.persistence.NoResultException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ttn.ck.apn.utils.ApnUtils.*;

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
        params.put(PARTNER_NAME, partnerName);
        return QueryExecutor.getDynamicResult(queries.getGetAllCustomers(), params, CustomerData.class);
    }

    @Override
    public int addCustomer(CustomerData data) {
        log.debug("Inserting new customer communication record: {}", data.getCustomerName());
        Map<String, Object> params = buildCustomerParams(data);
        params.put(PARTNER_NAME, data.getPartnerName());
        int rows = PersistenceQueryExecutor.save(queries.getAddCustomer(), params);
        log.info("Inserted {} customer communication record(s)", rows);
        return rows;
    }

    @Override
    public int updateCustomer(CustomerData data) {
        log.debug("Updating customer communication record ID: {}", data.getUuid());
        Map<String, Object> params = buildCustomerParams(data);
        params.put(UUID, data.getUuid());
        int rows = PersistenceQueryExecutor.save(queries.getUpdateCustomer(), params);
        log.info("Updated {} customer communication record(s)", rows);
        return rows;
    }

    @Override
    public int deleteCustomer(String uuid) {
        log.debug("Deleting customer communication record: {}", uuid);
        Map<String, Object> params = new HashMap<>();
        params.put(UUID, uuid);
        int rows = PersistenceQueryExecutor.save(queries.getDeleteCustomer(), params);
        log.info("Deleted {} customer communication record(s)", rows);
        return rows;
    }

    @Override
    public CustomerData findCustomerByUuid(String uuid) {
        log.debug("Fetching customer communication record: {}", uuid);
        Map<String, Object> params = new HashMap<>();
        params.put(UUID, uuid);
        return QueryExecutor.getDynamicSingleResult(queries.getByUuid(), params, CustomerData.class);
    }

    @Override
    public CustomerData findCustomerByNameAndPartner(String customerName, String partnerName) {
        log.debug("Fetching customer communication record: by customer {} and partner {}", customerName, partnerName);
        Map<String, Object> params = new HashMap<>();
        params.put(CUSTOMER_NAME, customerName);
        params.put(PARTNER_NAME, partnerName);
        try {
            return QueryExecutor.getDynamicSingleResult(queries.getByCustomerNameAndPartner(), params, CustomerData.class);
        } catch (NoResultException exception) {
            log.error("No record found for query", exception);
        }
        return null;
    }

    /**
     * Builds common parameter map from a CustomerData object.
     */
    private Map<String, Object> buildCustomerParams(CustomerData data) {
        Map<String, Object> params = new HashMap<>();
        params.put(CUSTOMER_NAME, data.getCustomerName());
        params.put(CUSTOMER_COMPANY_NAME, data.getCustomerCompanyName());
        params.put(INDUSTRY, data.getIndustry());
        params.put(INDUSTRY_OTHER, data.getIndustryOther());
        params.put(COUNTRY, data.getCountry());
        params.put(STATE, data.getState());
        params.put(POSTAL_CODE, data.getPostalCode());
        params.put(WEBSITE, data.getWebsite());
        return params;
    }
}

package com.ttn.ck.apn.dao;

import com.ttn.ck.apn.model.CustomerData;

import java.util.List;

/**
 * Data Access Object for Customer Communication data.
 * Handles all database operations for the customer_communication_data table
 * using native Snowflake queries via QueryExecutor.
 */
public interface CustomerCommunicationDataDao {

    /**
     * Fetch all customer communication records for a given partner.
     *
     * @param partnerName the partner name to filter by
     * @return list of customer data records
     */
    List<CustomerData> findAll(String partnerName);

    /**
     * Insert a new customer communication record.
     *
     * @param data the customer data to insert
     * @return number of rows affected
     */
    int addCustomer(CustomerData data);

    /**
     * Update an existing customer communication record.
     *
     * @param data the customer data with updated fields
     * @return number of rows affected
     */
    int updateCustomer(CustomerData data);

    /**
     * Delete a customer communication record by customerName and partnerName.
     *
     * @param uuid the customer uuid to delete
     * @return number of rows affected
     */
    int deleteCustomer(String uuid);

    /**
     * Fetch a customer communication record by customerName and partnerName.
     *
     * @param uuid the customer uuid
     * @return a single customer data record, or null if not found
     */
    CustomerData findCustomerByUuid(String uuid);
}

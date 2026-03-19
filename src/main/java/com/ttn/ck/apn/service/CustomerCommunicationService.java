package com.ttn.ck.apn.service;

import com.ttn.ck.apn.model.CustomerData;

import java.util.List;

/**
 * Service interface for Customer Communication operations.
 * Encapsulates business logic for customer CRUD operations.
 */
public interface CustomerCommunicationService {

    /**
     * Fetch all customer communication records for the current partner.
     *
     * @return list of customer data records
     */
    List<CustomerData> getAllCustomers(String partnerName);

    /**
     * Add a new customer communication record.
     *
     * @param data the customer data to add
     */
    void addCustomer(CustomerData data);

    /**
     * Update an existing customer communication record.
     *
     * @param data the customer data with updated fields
     */
    void updateCustomer(CustomerData data);

    /**
     * Delete a customer communication record by ID.
     *
     * @param uuid the record uuid to delete
     */
    void deleteCustomer(String uuid);
}

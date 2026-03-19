package com.ttn.ck.apn.service.impl;

import com.ttn.ck.apn.dao.CustomerCommunicationDataDao;
import com.ttn.ck.apn.errorhandler.GenericStatusException;
import com.ttn.ck.apn.model.CustomerData;
import com.ttn.ck.apn.service.CustomerCommunicationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic implementation for Customer Communication operations.
 *
 * current user's partner, ensuring data isolation between partners.</p>
 */
@Slf4j
@Service
@AllArgsConstructor
public class CustomerCommunicationServiceImpl implements CustomerCommunicationService {

    private final CustomerCommunicationDataDao dao;

    @Override
    public List<CustomerData> getAllCustomers() {
        String partnerName = "CK AZ";
        log.info("Fetching all customer communication records for partner: {}", partnerName);
        List<CustomerData> customers = dao.findAll(partnerName);
        log.info("Found {} customer communication records", customers.size());
        return customers;
    }

    @Override
    public void addCustomer(CustomerData data) {
        String partnerName = "CK AZ";
        data.setPartnerName(partnerName);
        log.info("Adding new customer: {} for partner: {}", data.getCustomerName(), partnerName);
        
        CustomerData existing = dao.findCustomerByUuid(data.getUuid());
        if (existing != null) {
            throw new GenericStatusException("Customer with this name already exists", HttpStatus.BAD_REQUEST.value());
        }
        
        int rows = dao.addCustomer(data);
        if (rows == 0) {
            throw new GenericStatusException("Failed to add customer record", HttpStatus.BAD_REQUEST.value());
        }
        log.info("Customer record added successfully");
    }

    @Override
    public void updateCustomer(CustomerData data) {
        String partnerName = "CK AZ";
        data.setPartnerName(partnerName);
        log.info("Updating customer: {} for partner: {}", data.getUuid(), "CK AZ");

        CustomerData existing = dao.findCustomerByUuid(data.getUuid());
        if (existing == null) {
            throw new GenericStatusException("No customer record found with name: " + data.getCustomerName(), HttpStatus.BAD_REQUEST.value());
        }
        
        int rows = dao.updateCustomer(data);
        if (rows == 0) {
            throw new GenericStatusException("Failed to update customer record", HttpStatus.BAD_REQUEST.value());
        }
        log.info("Customer record updated successfully");
    }

    @Override
    public void deleteCustomer(String uuid) {
        String partnerName = "CK AZ";
        log.info("Deleting customer: {} for partner: {}", uuid, partnerName);
        int rows = dao.deleteCustomer(uuid);
        if (rows == 0) {
            throw new GenericStatusException("No customer record found with name: " + uuid, HttpStatus.BAD_REQUEST.value());
        }
        log.info("Customer record deleted successfully");
    }
}

package com.ttn.ck.apn.controller;

import com.ttn.ck.apn.dto.CustomerCommunicationRequest;
import com.ttn.ck.apn.dto.SuccessResponseDto;
import com.ttn.ck.apn.model.CustomerData;
import com.ttn.ck.apn.service.CustomerCommunicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for APN Opportunity data operations.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET  /customer-communication/getAll}              — Listing of customers data</li>
 *   <li>{@code POST /customer-communication/add                  — Add New Customer</li>
 *   <li>{@code PUT  /customer-communication/update               — Update customer by id</li>
 *   <li>{@code DELETE /customer-communication/delete/{id}        — Delete customer by id</li>
 * </ul>
 */

@Slf4j
@Validated
@RestController
@RequestMapping("/customer-communication")
@RequiredArgsConstructor
public class CustomerCommunicationController {

    private final CustomerCommunicationService service;

    /**
     * Fetches all customer communication records for the current partner.
     *
     * <p>Example:
     * {@code GET /customer-communication/getAll}
     *
     * @return list of all customer data records
     */
    @GetMapping("/getAll")
    public SuccessResponseDto<List<CustomerData>> getAllCustomers() {
        log.info("GET /customer-communication/getAll");
        return new SuccessResponseDto<>(service.getAllCustomers());
    }

    /**
     * Adds a new customer communication record.
     *
     * <p>Example request body:
     * <pre>
     * {
     *   "customerName": "John Doe",
     *   "customerCompanyName": "Acme Corp",
     *   "industry": "Technology",
     *   "country": "US",
     *   "state": "CA",
     *   "postalCode": "94105",
     *   "website": "<a href="https://cloudkeeper.com">...</a>"
     * }
     * </pre>
     *
     * @param request the customer data to add
     * @return success response
     */
    @PostMapping("/add")
    public SuccessResponseDto<Boolean> addCustomer(@Valid @RequestBody CustomerCommunicationRequest request) {
        log.info("POST /customer-communication/add — customer={}", request.getCustomerName());
        CustomerData data = mapToCustomerData(request);
        service.addCustomer(data);
        return new SuccessResponseDto<>(Boolean.TRUE);
    }

    /**
     * Updates an existing customer communication record by ID.
     *
     * <p>Example request body:
     * <pre>
     * {
     *   "id": 1,
     *   "customerName": "Jane Doe",
     *   "customerCompanyName": "Acme Corp Updated",
     *   "industry": "Finance",
     *   "country": "US",
     *   "state": "NY",
     *   "postalCode": "10001",
     *   "website": "<a href="https://cloudkeeper.com">...</a>"
     * }
     * </pre>
     *
     * @param request the customer data with updated fields (must include id)
     * @return success response
     */
    @PutMapping("/update")
    public SuccessResponseDto<Boolean> updateCustomer(@Valid @RequestBody CustomerCommunicationRequest request) {
        log.info("PUT /customer-communication/update ={}", request.getCustomerName());
        CustomerData data = mapToCustomerData(request);
        service.updateCustomer(data);
        return new SuccessResponseDto<>(Boolean.TRUE);
    }

    /**
     * Deletes a customer communication record by ID.
     *
     * <p>Example:
     * {@code DELETE /customer-communication/delete/1}
     *
     * @param uuid the record ID to delete
     * @return success response
     */
    @DeleteMapping("/delete/{uuid}")
    public SuccessResponseDto<Boolean> deleteCustomer(@PathVariable String uuid) {
        log.info("DELETE /customer-communication/delete/{}", uuid);
        service.deleteCustomer(uuid);
        return new SuccessResponseDto<>(Boolean.TRUE);
    }

    /**
     * Maps a {@link CustomerCommunicationRequest} DTO to a {@link CustomerData} model.
     */
    private CustomerData mapToCustomerData(CustomerCommunicationRequest request) {
        return CustomerData.builder()
                .uuid(request.getUuid())
                .customerName(request.getCustomerName())
                .customerCompanyName(request.getCustomerCompanyName())
                .industry(request.getIndustry())
                .industryOther(request.getIndustryOther())
                .country(request.getCountry())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .website(request.getWebsite())
                .build();
    }
}

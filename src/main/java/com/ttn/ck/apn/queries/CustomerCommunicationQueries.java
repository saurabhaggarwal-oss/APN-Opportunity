package com.ttn.ck.apn.queries;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static lombok.AccessLevel.PRIVATE;

@Data
@Configuration
@PropertySource("classpath:queries/customer-communication-queries.properties")
@FieldDefaults(level = PRIVATE)
public class CustomerCommunicationQueries {

    @Value("${customer.communication.getAll}")
    String getAllCustomers;

    @Value("${customer.communication.add}")
    String addCustomer;

    @Value("${customer.communication.update}")
    String updateCustomer;

    @Value("${customer.communication.delete}")
    String deleteCustomer;

    @Value("${customer.communication.getByUuid}")
    String byUuid;

}
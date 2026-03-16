package com.ttn.ck.apn.queries;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static lombok.AccessLevel.PRIVATE;

@Data
@Configuration
@PropertySource("classpath:queries/customer-communication-queries.properties")
@FieldDefaults(level = PRIVATE)
public class CustomerCommunicationQueries {

}
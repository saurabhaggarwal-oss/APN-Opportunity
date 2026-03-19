package com.ttn.ck.datasourceconfig;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class SnowflakeConfigProperties {
    @Value("${spring.datasource.url}")
    private String databaseUrl;
    @Value("${spring.datasource.name}")
    private String databaseName;
    @Value("${spring.datasource.warehouse}")
    private String warehouse;
    @Value("${spring.datasource.schema}")
    private String schema;
    @Value("${spring.datasource.role}")
    private String role;
    @Value("${spring.datasource.username}")
    private String userName;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.driver-class-name}")
    private String databaseDriver;
    @Value("${spring.datasource.caseSensitivity}")
    private String caseSensitivity;
    @Value("${spring.datasource.maxActive}")
    private String maxActive;
    @Value("${spring.datasource.dialect}")
    private String dialect;

}

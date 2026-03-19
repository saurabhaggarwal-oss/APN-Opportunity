package com.ttn.ck.datasourceconfig;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@AllArgsConstructor
@Configuration
@DependsOn({"beanInstance","cacheableDatasource","snowflakeConfigProperties"})
public class DataSourceConfig {

    private final SnowflakeConfigProperties snowflakeConfigProperties;
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(snowflakeConfigProperties.getDatabaseDriver());
        dataSource.setUrl(snowflakeConfigProperties.getDatabaseUrl());
        dataSource.setUsername(snowflakeConfigProperties.getUserName());
        dataSource.setPassword(snowflakeConfigProperties.getPassword());
        return dataSource;
    }
}
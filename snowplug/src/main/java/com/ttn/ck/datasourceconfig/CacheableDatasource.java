package com.ttn.ck.datasourceconfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.snowflake.client.jdbc.internal.google.common.base.Strings;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.ttn.ck.constants.SnowPlugConstants.MAX_ACTIVE_CONNECTION;
import static com.ttn.ck.constants.SnowPlugConstants.MULTI_TENANT_CACHE_VALUE;

@Slf4j
@Component("cacheableDatasource")
@RequiredArgsConstructor
public class CacheableDatasource {

    private final SnowflakeConfigProperties snowflakeConfigProperties;

    public DataSource getDataSource() {
        String connectionUrl = getConnectionUrl();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(snowflakeConfigProperties.getDatabaseDriver());
        dataSource.setUrl(connectionUrl);
        dataSource.setUsername(snowflakeConfigProperties.getUserName());
        dataSource.setPassword(snowflakeConfigProperties.getPassword());
        dataSource.setConnectionProperties(getDatasourceProperties());
        return dataSource;
    }

    @Scheduled(fixedDelayString = "#{${snowflake.datasource.session.clear}}", timeUnit = TimeUnit.HOURS)
    @CacheEvict(value = {MULTI_TENANT_CACHE_VALUE})
    public void evictDataSourceCacheByKey() {
        log.info("DataSource Cache cleaned");
    }

    private Properties getDatasourceProperties() {
        Properties prop = new Properties();
        prop.setProperty(MAX_ACTIVE_CONNECTION,snowflakeConfigProperties.getMaxActive());
        return prop;
    }

    private String getConnectionUrl() {
        String tenant = snowflakeConfigProperties.getDatabaseName();
        String warehouse = snowflakeConfigProperties.getWarehouse();
        String schema = snowflakeConfigProperties.getSchema();
        String role = snowflakeConfigProperties.getRole();
        return Strings.isNullOrEmpty(role) ?
                getConnectionUrlWithoutRole(tenant, warehouse, schema) :
                getConnectionUrlWithoutRole(tenant, warehouse, schema) + "&role=" + role;
    }

    private String getConnectionUrlWithoutRole(String tenant, String warehouse, String schema) {
        return snowflakeConfigProperties.getDatabaseUrl() + "?warehouse=" + warehouse + "&db=" + tenant + "&schema=" +
                schema + "&CLIENT_RESULT_COLUMN_CASE_INSENSITIVE=" + snowflakeConfigProperties.getCaseSensitivity();
    }

}

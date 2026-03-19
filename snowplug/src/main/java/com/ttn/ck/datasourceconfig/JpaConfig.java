package com.ttn.ck.datasourceconfig;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

@AllArgsConstructor
@Configuration
public class JpaConfig {

    private final SnowflakeConfigProperties snowflakeConfigProperties;
    private final CacheableDatasource cacheableDatasource;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setDataSource(new MultiTenantDatasource(dataSource));
        emf.setJpaProperties(getSnowflakeDialect());
        return emf;
    }

    private Properties getSnowflakeDialect(){
        Properties prop = new Properties();
        prop.setProperty(Constants.HIBERNATE_DIALECT, snowflakeConfigProperties.getDialect());
        return prop;
    }

    private static class Constants {
        private Constants() {
        }

        public static final String HIBERNATE_DIALECT = "hibernate.dialect";
    }
}




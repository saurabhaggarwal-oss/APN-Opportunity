package com.ttn.ck.datasourceconfig;

import com.ttn.ck.queryprocessor.utils.Instance;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

public class MultiTenantDatasource extends AbstractRoutingDataSource {


    @Override
    protected @Nullable Object determineCurrentLookupKey() {
        return null;
    }

    public MultiTenantDatasource(){}
    public MultiTenantDatasource(DataSource defaultTargetDataSource) {
        setDefaultTargetDataSource(defaultTargetDataSource);
    }

    @Override
    @NullMarked
    protected DataSource determineTargetDataSource() {
        CacheableDatasource cacheableDatasource = Instance.of(CacheableDatasource.class);
        return cacheableDatasource.getDataSource();
    }

}

package com.ttn.ck.queryprocessor.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
class QueryManager {
    @PersistenceContext
    private EntityManager entityManager;
    private final Environment environment;
    private Boolean toLowerCase;

    public QueryManager(Environment environment) {
        this.environment = environment;
    }

    public <T> Query createNativeQuery(String qString, Class<T> convertTo) {
        return entityManager.createNativeQuery(qString, convertTo);
    }

    public NativeQuery<Tuple> createNativeQueryWithScroll(String qString) {
        NativeQuery<Tuple> nativeQuery = entityManager.unwrap(Session.class).createNativeQuery(qString, Tuple.class);
        nativeQuery.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
        return nativeQuery;
    }

    public Query createNativeQuery(String qString) {
        return entityManager.createNativeQuery(qString);
    }

    public <T> ScrollableResults createScrollableNativeQuery(String qString, Class<T> convertTo, Map<String, Object> params, ScrollMode mode) {
        NativeQuery<T> query = getSession().createNativeQuery(qString, convertTo);
        params.forEach(query::setParameter);
        return query.scroll(mode);
    }

    public ScrollableResults createScrollableNativeQuery(String qString, Map<String, Object> params, ScrollMode mode) {
        NativeQuery<?> query = getSession().createNativeQuery(qString);
        params.forEach(query::setParameter);
        return query.scroll(mode);
    }

    public <T> ScrollableResults createScrollableNativeQuery(String qString, Class<T> convertTo, Map<String, Object> params) {
        return createScrollableNativeQuery(qString, convertTo, params, ScrollMode.FORWARD_ONLY);
    }

    public ScrollableResults createScrollableNativeQuery(String qString, Map<String, Object> params) {
        return createScrollableNativeQuery(qString, params, ScrollMode.FORWARD_ONLY);
    }

    public Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    /**
     * this will return keys to lower case if toLowerCase is true and to uppercase if toLowerCase is false and as it is if toLowerCase is null.
     */
    public String getKey(String alias) {
        if (Objects.isNull(toLowerCase)) {
            return alias;
        }
        return Boolean.TRUE.equals(toLowerCase) ? alias.toLowerCase() : alias.toUpperCase();
    }

    @PostConstruct
    private void init() {
        try {
            String booleanString = environment.getProperty(CONSTANTS.TO_LOWER_CASE);
            if (StringUtils.hasText(booleanString)) {
                toLowerCase = Boolean.parseBoolean(environment.getProperty(CONSTANTS.TO_LOWER_CASE));
            }
        } catch (Exception e) {
            log.warn("Enable to set toLowerCase from environment with error:: {}", e.getMessage());
        }
    }

    private static class CONSTANTS {
        public static final String TO_LOWER_CASE = "QUERY.CASE_TO_LOWER";
    }
}

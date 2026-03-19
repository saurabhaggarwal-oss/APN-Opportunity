package com.ttn.ck.queryprocessor.utils;

import java.util.HashMap;
import java.util.Map;

public class QueryParamBuilder {

    private final Map<String, Object> queryParams;

    private QueryParamBuilder() {
        queryParams = new HashMap<>(0);
    }

    private QueryParamBuilder(int numberOfParams) {
        queryParams = new HashMap<>(numberOfParams);
    }

    public static QueryParamBuilder init() {
        return new QueryParamBuilder();
    }

    public static QueryParamBuilder init(int numberOfParams) {
        return new QueryParamBuilder(numberOfParams);
    }

    public QueryParamBuilder with(String paramName, Object value) {
        this.queryParams.put(paramName, value);
        return this;
    }

    public Map<String, Object> build() {
        return this.queryParams;
    }

}

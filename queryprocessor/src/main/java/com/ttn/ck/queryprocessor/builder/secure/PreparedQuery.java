package com.ttn.ck.queryprocessor.builder.secure;

import java.util.ArrayList;
import java.util.List;

public final class PreparedQuery {
    private final String sql;
    private final List<Object> params;
    public PreparedQuery(String sql, List<Object> params) {
        this.sql = sql;
        this.params = List.copyOf(new ArrayList<>(params));
    }
    public String getSql() { return sql; }
    public List<Object> getParams() { return params; }
}
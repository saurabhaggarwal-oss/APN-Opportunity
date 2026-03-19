package com.ttn.ck.queryprocessor.builder.secure;

import com.ttn.ck.queryprocessor.builder.model.Filter;
import com.ttn.ck.queryprocessor.builder.model.Operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class SecureFilter extends Filter {
    private final List<Object> params;

    private SecureFilter(String sql, List<Object> params) {
        super(new StringBuilder(sql));
        this.params = List.copyOf(params);
    }

    public List<Object> getParams() { return params; }

    public static SecureBuilder secureBuilder() { return new SecureBuilder(); }

    public static final class SecureBuilder {
        private final StringBuilder sb = new StringBuilder();
        private final List<Object> params = new ArrayList<>();

        public SecureBuilder and() { sb.append(" AND "); return this; }
        public SecureBuilder or() { sb.append(" OR "); return this; }
        public SecureBuilder open() { sb.append("("); return this; }
        public SecureBuilder close() { sb.append(") "); return this; }

        public SecureBuilder apply(String column, Operator op, Object value) {
            Objects.requireNonNull(column, "column");
            Objects.requireNonNull(op, "operator");
            switch (op) {
                case EQUALS -> bin(column, " = ", value);
                case NOT_EQUALS -> bin(column, " != ", value);
                case GREATER_THAN -> bin(column, " > ", value);
                case GREATER_THAN_OR_EQUAL -> bin(column, " >= ", value);
                case LESS_THAN -> bin(column, " < ", value);
                case LESS_THAN_OR_EQUAL -> bin(column, " <= ", value);
                case LIKE -> bin(column, " LIKE ", value);
                case NOT_LIKE -> bin(column, " NOT LIKE ", value);
                case STARTS_WITH -> bin(column, " LIKE ", String.valueOf(value) + "%");
                case ENDS_WITH -> bin(column, " LIKE ", "%" + String.valueOf(value));
                case CONTAINS -> bin(column, " LIKE ", "%" + String.valueOf(value) + "%");
                case NOT_CONTAINS -> bin(column, " NOT LIKE ", "%" + String.valueOf(value) + "%");
                case IN -> in(column, false, value);
                case NOT_IN -> in(column, true, value);
                case BETWEEN -> between(column, false, value);
                case NOT_BETWEEN -> between(column, true, value);
                case IS_NULL -> sb.append(column).append(" IS NULL ");
                case IS_NOT_NULL -> sb.append(column).append(" IS NOT NULL ");
                default -> throw new IllegalArgumentException("Unsupported operator: " + op);
            }
            return this;
        }

        private void bin(String col, String op, Object v) {
            sb.append(col).append(op).append("? ");
            params.add(v);
        }
        private void in(String col, boolean not, Object value) {
            if (!(value instanceof Collection<?> coll) || coll.isEmpty()) {
                sb.append(not ? " 1=1 " : " 1=0 "); // neutral/no-match fallback
                return;
            }
            sb.append(col).append(not ? " NOT IN (" : " IN (");
            boolean first = true;
            for (Object v : coll) {
                if (!first) sb.append(", ");
                sb.append("?");
                params.add(v);
                first = false;
            }
            sb.append(") ");
        }
        private void between(String col, boolean not, Object value) {
            if (!(value instanceof Object[] arr) || arr.length != 2) {
                throw new IllegalArgumentException("BETWEEN expects exactly 2 values");
            }
            sb.append(col).append(not ? " NOT BETWEEN " : " BETWEEN ").append("? AND ? ");
            params.add(arr[0]);
            params.add(arr[1]);
        }

        public SecureFilter build() {
            return new SecureFilter(sb.toString(), List.copyOf(params));
        }
    }
}

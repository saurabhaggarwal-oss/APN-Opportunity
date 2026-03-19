package com.ttn.ck.queryprocessor.builder.model;
import lombok.AllArgsConstructor;
import lombok.Data;

import static com.ttn.ck.queryprocessor.builder.utils.ApplicationConstants.*;

/**
 * Window Functions - Returns window function SQL that can be wrapped in Column
 */
@Data
@AllArgsConstructor
public class WindowFunction {
    private String functionName;
    private String partitionBy;
    private String orderBy;
    private String frameClause;
    private String alias;

    public static WindowBuilder builder() {
        return new WindowBuilder();
    }

    /**
     * Get SQL with alias (for direct use)
     */
    public String toSql() {
        StringBuilder sql = new StringBuilder();
        sql.append(toSqlWithoutAlias());

        if (alias != null && !alias.isEmpty()) {
            sql.append(AS).append(alias);
        }

        return sql.toString();
    }

    /**
     * Get SQL without alias (for wrapping in Column)
     */
    public String toSqlWithoutAlias() {
        StringBuilder sql = new StringBuilder();
        sql.append(functionName).append(" OVER (");

        if (partitionBy != null && !partitionBy.isEmpty()) {
            sql.append("PARTITION BY ").append(partitionBy);
        }

        if (orderBy != null && !orderBy.isEmpty()) {
            if (partitionBy != null && !partitionBy.isEmpty()) {
                sql.append(SPACE);
            }
            sql.append("ORDER BY ").append(orderBy);
        }

        if (frameClause != null && !frameClause.isEmpty()) {
            sql.append(SPACE).append(frameClause);
        }

        sql.append(")");
        return sql.toString();
    }

    public static class WindowBuilder {
        private String functionName;
        private String partitionBy;
        private String orderBy;
        private String frameClause;
        private String alias;

        // ROW_NUMBER, RANK, DENSE_RANK, etc. - same as before
        public WindowBuilder rowNumber() {
            this.functionName = "ROW_NUMBER()";
            return this;
        }

        public WindowBuilder rank() {
            this.functionName = "RANK()";
            return this;
        }

        public WindowBuilder denseRank() {
            this.functionName = "DENSE_RANK()";
            return this;
        }

        public WindowBuilder ntile(int buckets) {
            this.functionName = "NTILE(" + buckets + ")";
            return this;
        }

        public WindowBuilder lag(String column) {
            this.functionName = "LAG(" + column + ")";
            return this;
        }

        public WindowBuilder lead(String column) {
            this.functionName = "LEAD(" + column + ")";
            return this;
        }

        public WindowBuilder sum(String column) {
            this.functionName = "SUM(" + column + ")";
            return this;
        }

        public WindowBuilder avg(String column) {
            this.functionName = "AVG(" + column + ")";
            return this;
        }

        public WindowBuilder count(String column) {
            this.functionName = "COUNT(" + column + ")";
            return this;
        }

        public WindowBuilder partitionBy(String... columns) {
            this.partitionBy = String.join(COMMA_DELIMETER, columns);
            return this;
        }

        public WindowBuilder orderBy(String... columns) {
            this.orderBy = String.join(COMMA_DELIMETER, columns);
            return this;
        }

        public WindowBuilder orderByAsc(String column) {
            this.orderBy = column + " ASC";
            return this;
        }

        public WindowBuilder orderByDesc(String column) {
            this.orderBy = column + " DESC";
            return this;
        }

        public WindowBuilder rowsBetweenUnboundedPrecedingAndCurrentRow() {
            this.frameClause = "ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW";
            return this;
        }

        public WindowBuilder as(String alias) {
            this.alias = alias;
            return this;
        }

        public WindowFunction build() {
            return new WindowFunction(functionName, partitionBy, orderBy, frameClause, alias);
        }
    }
}
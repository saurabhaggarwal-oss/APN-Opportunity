package com.ttn.ck.queryprocessor.builder.service;

import com.ttn.ck.queryprocessor.builder.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.ttn.ck.queryprocessor.builder.utils.ApplicationConstants.*;

/**
 * Simplified QueryBuilder - ONLY accepts Column objects for SELECT
 * This creates a clean, consistent API where everything flows through Column
 * <p>
 * Philosophy: Column is the universal container for SELECT items
 */
@Data
@AllArgsConstructor
public class QueryBuilder {
    private CTE cte;
    private String tableName;
    private String tableAlias;
    private List<Column> columns;
    private Filter filter;
    private GroupBy groupBy;
    private Having having;
    private OrderBy orderBy;
    private Join join;
    private Integer limit;
    private Integer offset;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CTE cte;
        private String tableName;
        private String tableAlias;
        private final List<Column> columns = new ArrayList<>();
        private Filter filter;
        private GroupBy groupBy;
        private Having having;
        private OrderBy orderBy;
        private Join join;
        private Integer limit;
        private Integer offset;

        // ============================================================
        // CTE (WITH clause)
        // ============================================================

        /**
         * Add CTE (WITH clause)
         */
        public Builder with(CTE cte) {
            this.cte = cte;
            return this;
        }

        // ============================================================
        // FROM clause
        // ============================================================

        /**
         * Set table name
         */
        public Builder from(String tableName) {
            this.tableName = tableName;
            return this;
        }

        /**
         * Set table with alias
         */
        public Builder from(String tableName, String alias) {
            this.tableName = tableName;
            this.tableAlias = alias;
            return this;
        }

        /**
         * Set table with alias
         */
        public Builder from(String schema, String tableName, String alias) {
            this.tableName = qualify(schema, tableName);
            this.tableAlias = alias;
            return this;
        }

        // ============================================================
        // SELECT - ONLY Column objects
        // ============================================================

        /**
         * Add a single Column
         */
        public Builder select(Column column) {
            this.columns.add(column);
            return this;
        }

        /**
         * Add multiple Column objects
         */
        public Builder select(Column... columns) {
            this.columns.addAll(Arrays.asList(columns));
            return this;
        }

        /**
         * Add collection of Column objects
         */
        public Builder select(Collection<Column> columns) {
            this.columns.addAll(columns);
            return this;
        }

        // ============================================================
        // WHERE clause
        // ============================================================

        /**
         * WHERE clause
         */
        public Builder where(Filter filter) {
            this.filter = filter;
            return this;
        }

        // ============================================================
        // JOIN clauses
        // ============================================================

        /**
         * JOINs
         */
        public Builder joins(Join join) {
            this.join = join;
            return this;
        }


        // ============================================================
        // GROUP BY clause
        // ============================================================

        /**
         * GROUP BY clause
         */
        public Builder groupBy(GroupBy groupBy) {
            this.groupBy = groupBy;
            return this;
        }

        // ============================================================
        // HAVING clause
        // ============================================================

        /**
         * HAVING clause
         */
        public Builder having(Having having) {
            this.having = having;
            return this;
        }

        // ============================================================
        // ORDER BY clause
        // ============================================================

        /**
         * ORDER BY clause
         */
        public Builder orderBy(OrderBy orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        // ============================================================
        // LIMIT and OFFSET
        // ============================================================

        /**
         * LIMIT
         */
        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        /**
         * OFFSET
         */
        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        // ============================================================
        // BUILD
        // ============================================================

        /**
         * Build the final SQL query
         */
        public String build() {
            StringBuilder sql = new StringBuilder();

            // CTE (WITH clause)
            if (cte != null && !cte.isEmpty()) {
                sql.append(cte.toSql());
            }

            // SELECT
            sql.append(SELECT);
            if (columns.isEmpty()) {
                sql.append("*");
            } else {
                sql.append(columns.stream().map(Column::toSql).collect(Collectors.joining(COMMA_DELIMETER)));
            }

            // FROM
            if (tableName != null && !tableName.isEmpty()) {
                sql.append(FROM).append(tableName);
                if (tableAlias != null && !tableAlias.isEmpty()) {
                    sql.append(SPACE).append(tableAlias);
                }
            }

            // JOINs
            if (join != null && !join.isEmpty()) {
                sql.append(join.toSql());
            }

            // WHERE
            if (filter != null && !filter.toSql().isEmpty()) {
                sql.append(WHERE).append(filter.toSql());
            }

            // GROUP BY
            if (groupBy != null && !groupBy.isEmpty()) {
                sql.append(groupBy.toSql());
            }

            // HAVING
            if (having != null && !having.isEmpty()) {
                sql.append(having.toSql());
            }

            // ORDER BY
            if (orderBy != null && !orderBy.isEmpty()) {
                sql.append(orderBy.toSql());
            }

            // LIMIT
            if (limit != null) {
                sql.append(SPACE).append("LIMIT").append(SPACE).append(limit);
            }

            // OFFSET
            if (offset != null) {
                sql.append(SPACE).append("OFFSET").append(SPACE).append(offset);
            }

            return sql.toString();
        }

        /**
         * Build INSERT query
         */
        public String buildInsert(String placeHolders) {
            StringBuilder sql = new StringBuilder();

            sql.append(INSERT_INTO).append(tableName).append(OPEN_BRACKET);

            if (!columns.isEmpty()) {
                sql.append(columns.stream().map(Column::getExpression).collect(Collectors.joining(COMMA_DELIMETER)));
            }

            sql.append(CLOSE_BRACKET).append(VALUES).append(OPEN_BRACKET).append(placeHolders).append(CLOSE_BRACKET);

            return sql.toString();
        }

        /**
         * Build UPDATE query
         */
        public String buildUpdate(String setClause) {
            StringBuilder sql = new StringBuilder();

            sql.append(UPDATE).append(tableName).append(SET).append(setClause);

            // WHERE
            if (filter != null && !filter.toSql().isEmpty()) {
                sql.append(WHERE).append(filter.toSql());
            }

            return sql.toString();
        }

        /**
         * Build DELETE query
         */
        public String buildDelete() {
            StringBuilder sql = new StringBuilder();

            sql.append("DELETE").append(FROM).append(tableName);

            // WHERE
            if (filter != null && !filter.toSql().isEmpty()) {
                sql.append(WHERE).append(filter.toSql());
            }

            return sql.toString();
        }

        /**
         * Build COUNT query
         */
        public String buildCount() {
            StringBuilder sql = new StringBuilder();

            sql.append(SELECT).append("COUNT(*)").append(FROM).append(tableName);

            if (tableAlias != null && !tableAlias.isEmpty()) {
                sql.append(SPACE).append(tableAlias);
            }

            // JOINs
            if (join != null && !join.isEmpty()) {
                sql.append(join.toSql());
            }

            // WHERE
            if (filter != null && !filter.toSql().isEmpty()) {
                sql.append(WHERE).append(filter.toSql());
            }

            return sql.toString();
        }

        private String qualify(String schema, String table) {
            return (schema == null || schema.isBlank()) ? table : schema + "." + table;
        }
    }

}
package com.ttn.ck.queryprocessor.builder.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import static com.ttn.ck.queryprocessor.utils.ApplicationConstants.*;

/**
 * Represents a subquery that can be used in SELECT, FROM, WHERE, or JOIN clauses
 * Much simpler than Hibernate's Subquery API
 */
@Data
@AllArgsConstructor
public class SubQuery {
    private String query;
    private String alias;
    
    public SubQuery(String query) {
        this.query = query;
        this.alias = null;
    }
    
    public static SubQueryBuilder builder() {
        return new SubQueryBuilder();
    }
    
    public String toSql() {
        StringBuilder sql = new StringBuilder();
        sql.append(OPEN_BRACKET).append(query).append(CLOSE_BRACKET);
        
        if (alias != null && !alias.isEmpty()) {
            sql.append(AS).append(alias);
        }
        
        return sql.toString();
    }
    
    public String toSqlWithoutParentheses() {
        return query;
    }
    
    public static class SubQueryBuilder {
        private String query;
        private String alias;
        
        /**
         * Set the subquery SQL
         */
        public SubQueryBuilder query(String sql) {
            this.query = sql;
            return this;
        }
        
        /**
         * Build from QueryBuilder
         */
        public SubQueryBuilder fromQueryBuilder(String builtQuery) {
            this.query = builtQuery;
            return this;
        }
        
        /**
         * Set alias for the subquery
         */
        public SubQueryBuilder as(String alias) {
            this.alias = alias;
            return this;
        }
        
        public SubQuery build() {
            return new SubQuery(query, alias);
        }
    }
    
    /**
     * Static factory methods for common subquery patterns
     */
    
    /**
     * Create a scalar subquery (returns single value)
     */
    public static SubQuery scalar(String query) {
        return new SubQuery(query);
    }
    
    /**
     * Create a subquery for use in FROM clause
     */
    public static SubQuery fromSubquery(String query, String alias) {
        return new SubQuery(query, alias);
    }
    
    /**
     * Create a subquery for use in EXISTS
     */
    public static String exists(SubQuery subQuery) {
        return "EXISTS " + subQuery.toSql();
    }
    
    /**
     * Create a subquery for use in NOT EXISTS
     */
    public static String notExists(SubQuery subQuery) {
        return "NOT EXISTS " + subQuery.toSql();
    }
    
    /**
     * Create a subquery for use in IN clause
     */
    public static String in(String column, SubQuery subQuery) {
        return column + " IN " + subQuery.toSql();
    }
    
    /**
     * Create a subquery for use in NOT IN clause
     */
    public static String notIn(String column, SubQuery subQuery) {
        return column + " NOT IN " + subQuery.toSql();
    }
}
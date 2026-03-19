package com.ttn.ck.queryprocessor.builder.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static com.ttn.ck.queryprocessor.utils.ApplicationConstants.SPACE;

/**
 * Represents UNION, UNION ALL, INTERSECT, EXCEPT operations
 * Combines multiple SELECT queries
 */
@Data
@AllArgsConstructor
public class SetOperation {
    private List<SetClause> clauses;
    
    public SetOperation() {
        this.clauses = new ArrayList<>();
    }
    
    public static SetOperationBuilder builder() {
        return new SetOperationBuilder();
    }
    
    public String toSql() {
        if (clauses == null || clauses.isEmpty()) {
            return "";
        }
        
        StringBuilder sql = new StringBuilder();
        
        for (int i = 0; i < clauses.size(); i++) {
            if (i > 0) {
                sql.append(SPACE).append(clauses.get(i).getOperationType()).append(SPACE);
            }
            sql.append(clauses.get(i).getQuery());
        }
        
        return sql.toString();
    }
    
    /**
     * Enum for set operation types
     */
    public enum OperationType {
        UNION("UNION"),
        UNION_ALL("UNION ALL"),
        INTERSECT("INTERSECT"),
        EXCEPT("EXCEPT"),
        MINUS("MINUS");
        
        private final String sql;
        
        OperationType(String sql) {
            this.sql = sql;
        }
        
        public String getSql() {
            return sql;
        }
    }
    
    /**
     * Inner class representing a single clause in the set operation
     */
    @Data
    @AllArgsConstructor
    public static class SetClause {
        private String query;
        private OperationType operationType;
        
        public SetClause(String query) {
            this.query = query;
            this.operationType = OperationType.UNION; // Default
        }
    }
    
    public static class SetOperationBuilder {
        private final List<SetClause> clauses = new ArrayList<>();
        
        /**
         * Add first query (no operation type)
         */
        public SetOperationBuilder query(String query) {
            if (clauses.isEmpty()) {
                this.clauses.add(new SetClause(query, null));
            }
            return this;
        }
        
        /**
         * Add UNION query
         */
        public SetOperationBuilder union(String query) {
            this.clauses.add(new SetClause(query, OperationType.UNION));
            return this;
        }
        
        /**
         * Add UNION ALL query
         */
        public SetOperationBuilder unionAll(String query) {
            this.clauses.add(new SetClause(query, OperationType.UNION_ALL));
            return this;
        }
        
        /**
         * Add INTERSECT query
         */
        public SetOperationBuilder intersect(String query) {
            this.clauses.add(new SetClause(query, OperationType.INTERSECT));
            return this;
        }
        
        /**
         * Add EXCEPT query
         */
        public SetOperationBuilder except(String query) {
            this.clauses.add(new SetClause(query, OperationType.EXCEPT));
            return this;
        }
        
        /**
         * Add MINUS query (Oracle syntax)
         */
        public SetOperationBuilder minus(String query) {
            this.clauses.add(new SetClause(query, OperationType.MINUS));
            return this;
        }
        
        public SetOperation build() {
            return new SetOperation(clauses);
        }
    }
}
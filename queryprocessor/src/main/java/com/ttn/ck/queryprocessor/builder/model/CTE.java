package com.ttn.ck.queryprocessor.builder.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static com.ttn.ck.queryprocessor.utils.ApplicationConstants.*;

/**
 * Represents Common Table Expressions (WITH clause / CTEs)
 * Hibernate doesn't natively support CTEs - this makes it super easy!
 */
@Data
@AllArgsConstructor
public class CTE {
    private List<CTEDefinition> cteDefinitions;
    
    public CTE() {
        this.cteDefinitions = new ArrayList<>();
    }
    
    public static CTEBuilder builder() {
        return new CTEBuilder();
    }
    
    public String toSql() {
        if (cteDefinitions == null || cteDefinitions.isEmpty()) {
            return "";
        }
        
        StringBuilder sql = new StringBuilder("WITH ");
        
        for (int i = 0; i < cteDefinitions.size(); i++) {
            if (i > 0) {
                sql.append(COMMA_DELIMETER);
            }
            sql.append(cteDefinitions.get(i).toSql());
        }
        
        sql.append(SPACE);
        return sql.toString();
    }
    
    public boolean isEmpty() {
        return cteDefinitions == null || cteDefinitions.isEmpty();
    }
    
    /**
     * Inner class representing a single CTE definition
     */
    @Data
    @AllArgsConstructor
    public static class CTEDefinition {
        private String name;
        private List<String> columnNames;
        private String query;
        private boolean recursive;
        
        public CTEDefinition(String name, String query) {
            this.name = name;
            this.query = query;
            this.columnNames = null;
            this.recursive = false;
        }
        
        public String toSql() {
            StringBuilder sql = new StringBuilder();
            
            sql.append(name);
            
            // Add column names if specified
            if (columnNames != null && !columnNames.isEmpty()) {
                sql.append(OPEN_BRACKET)
                   .append(String.join(COMMA_DELIMETER, columnNames))
                   .append(CLOSE_BRACKET);
            }
            
            sql.append(" AS ").append(OPEN_BRACKET)
               .append(query)
               .append(CLOSE_BRACKET);
            
            return sql.toString();
        }
    }
    
    public static class CTEBuilder {
        private final List<CTEDefinition> cteDefinitions = new ArrayList<>();
        private boolean recursive = false;
        
        /**
         * Mark as recursive CTE
         */
        public CTEBuilder recursive() {
            this.recursive = true;
            return this;
        }
        
        /**
         * Add a CTE definition
         */
        public CTEBuilder with(String name, String query) {
            this.cteDefinitions.add(new CTEDefinition(name, query));
            return this;
        }
        
        /**
         * Add a CTE definition with column names
         */
        public CTEBuilder with(String name, List<String> columnNames, String query) {
            this.cteDefinitions.add(new CTEDefinition(name, columnNames, query, recursive));
            return this;
        }
        
        /**
         * Add a CTE from QueryBuilder result
         */
        public CTEBuilder withQueryBuilder(String name, String builtQuery) {
            this.cteDefinitions.add(new CTEDefinition(name, builtQuery));
            return this;
        }
        
        public CTE build() {
            // If recursive, modify the first CTE
            if (recursive && !cteDefinitions.isEmpty()) {
                cteDefinitions.get(0).setRecursive(true);
            }
            return new CTE(cteDefinitions);
        }
    }
}
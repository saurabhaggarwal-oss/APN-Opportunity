package com.ttn.ck.queryprocessor.builder.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static com.ttn.ck.queryprocessor.utils.ApplicationConstants.*;

/**
 * Represents ORDER BY clause in SQL query with support for ASC/DESC
 */
@Data
@AllArgsConstructor
public class OrderBy {
    private List<OrderByColumn> columns;
    
    public OrderBy() {
        this.columns = new ArrayList<>();
    }
    
    public static OrderByBuilder builder() {
        return new OrderByBuilder();
    }
    
    public String toSql() {
        if (columns == null || columns.isEmpty()) {
            return "";
        }
        List<String> columnStrings = new ArrayList<>();
        for (OrderByColumn col : columns) {
            columnStrings.add(col.toSql());
        }
        return ORDER_BY + String.join(COMMA_DELIMETER, columnStrings);
    }
    
    public boolean isEmpty() {
        return columns == null || columns.isEmpty();
    }
    
    /**
     * Inner class representing a single column in ORDER BY
     */
    @Data
    @AllArgsConstructor
    public static class OrderByColumn {
        private String columnName;
        private SortOrder sortOrder;
        
        public OrderByColumn(String columnName) {
            this.columnName = columnName;
            this.sortOrder = SortOrder.ASC;
        }
        
        public String toSql() {
            return columnName + SPACE + sortOrder.name();
        }
    }
    
    /**
     * Enum for sort order
     */
    public enum SortOrder {
        ASC, DESC
    }
    
    public static class OrderByBuilder {
        private final List<OrderByColumn> columns = new ArrayList<>();
        
        /**
         * Add column with default ASC order
         */
        public OrderByBuilder column(String columnName) {
            this.columns.add(new OrderByColumn(columnName, SortOrder.ASC));
            return this;
        }
        
        /**
         * Add column with specified order
         */
        public OrderByBuilder column(String columnName, SortOrder order) {
            this.columns.add(new OrderByColumn(columnName, order));
            return this;
        }
        
        /**
         * Add column in ascending order
         */
        public OrderByBuilder asc(String columnName) {
            this.columns.add(new OrderByColumn(columnName, SortOrder.ASC));
            return this;
        }
        
        /**
         * Add column in descending order
         */
        public OrderByBuilder desc(String columnName) {
            this.columns.add(new OrderByColumn(columnName, SortOrder.DESC));
            return this;
        }
        
        /**
         * Add multiple columns with default ASC order
         */
        public OrderByBuilder columns(String... columnNames) {
            for (String columnName : columnNames) {
                this.columns.add(new OrderByColumn(columnName, SortOrder.ASC));
            }
            return this;
        }
        
        /**
         * Add columns from list with default ASC order
         */
        public OrderByBuilder columns(List<String> columnNames) {
            for (String columnName : columnNames) {
                this.columns.add(new OrderByColumn(columnName, SortOrder.ASC));
            }
            return this;
        }
        
        /**
         * Parse and add from raw string (e.g., "column1 ASC, column2 DESC")
         */
        public OrderByBuilder fromString(String orderByString) {
            String[] parts = orderByString.split(COMMA_DELIMETER);
            for (String part : parts) {
                part = part.trim();
                String[] tokens = part.split("\\s+");
                
                if (tokens.length == 1) {
                    this.columns.add(new OrderByColumn(tokens[0], SortOrder.ASC));
                } else if (tokens.length >= 2) {
                    SortOrder order = tokens[1].equalsIgnoreCase("DESC") ? 
                        SortOrder.DESC : SortOrder.ASC;
                    this.columns.add(new OrderByColumn(tokens[0], order));
                }
            }
            return this;
        }
        
        public OrderBy build() {
            return new OrderBy(columns);
        }
    }
}

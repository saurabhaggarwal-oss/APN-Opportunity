package com.ttn.ck.queryprocessor.builder.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.ttn.ck.queryprocessor.utils.ApplicationConstants.COMMA_DELIMETER;
import static com.ttn.ck.queryprocessor.utils.ApplicationConstants.GROUP_BY;

/**
 * Represents GROUP BY clause in SQL query
 */
@Data
@AllArgsConstructor
public class GroupBy {
    private List<String> columns;
    
    public GroupBy() {
        this.columns = new ArrayList<>();
    }
    
    public static GroupByBuilder builder() {
        return new GroupByBuilder();
    }
    
    public String toSql() {
        if (columns == null || columns.isEmpty()) {
            return "";
        }
        return GROUP_BY + String.join(COMMA_DELIMETER, columns);
    }
    
    public boolean isEmpty() {
        return columns == null || columns.isEmpty();
    }
    
    public static class GroupByBuilder {
        private final List<String> columns = new ArrayList<>();
        
        /**
         * Add a single column to GROUP BY
         */
        public GroupByBuilder column(String columnName) {
            this.columns.add(columnName);
            return this;
        }
        
        /**
         * Add multiple columns to GROUP BY
         */
        public GroupByBuilder columns(String... columnNames) {
            this.columns.addAll(Arrays.asList(columnNames));
            return this;
        }
        
        /**
         * Add multiple columns from a list
         */
        public GroupByBuilder columns(List<String> columnNames) {
            this.columns.addAll(columnNames);
            return this;
        }
        
        /**
         * Add columns from comma-separated string
         */
        public GroupByBuilder columnsFromString(String commaSeparated) {
            this.columns.addAll(
                Arrays.stream(commaSeparated.split(COMMA_DELIMETER))
                    .map(String::trim)
                    .collect(Collectors.toList())
            );
            return this;
        }
        
        public GroupBy build() {
            return new GroupBy(columns);
        }
    }
}

package com.ttn.ck.queryprocessor.builder.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.ttn.ck.queryprocessor.utils.ApplicationConstants.*;

/**
 * Represents SQL expressions with support for mathematical, string, date operations
 * Much simpler and more intuitive than Hibernate's CriteriaBuilder expressions
 */
@Data
@AllArgsConstructor
public class Expression {
    private String expressionText;

    
    public static ExpressionBuilder builder() {
        return new ExpressionBuilder();
    }
    
    public String toSql() {
        return expressionText;
    }
    
    public static class ExpressionBuilder {
        private StringBuilder expression = new StringBuilder();
        
        /**
         * Create expression from raw SQL
         */
        public Expression raw(String sql) {
            return new Expression(sql);
        }
        
        /**
         * Mathematical Operations
         */
        public ExpressionBuilder add(String col1, String col2) {
            expression.append(OPEN_BRACKET).append(col1).append(" + ").append(col2).append(CLOSE_BRACKET);
            return this;
        }
        
        public ExpressionBuilder subtract(String col1, String col2) {
            expression.append(OPEN_BRACKET).append(col1).append(" - ").append(col2).append(CLOSE_BRACKET);
            return this;
        }
        
        public ExpressionBuilder multiply(String col1, String col2) {
            expression.append(OPEN_BRACKET).append(col1).append(" * ").append(col2).append(CLOSE_BRACKET);
            return this;
        }
        
        public ExpressionBuilder divide(String col1, String col2) {
            expression.append(OPEN_BRACKET).append(col1).append(" / ").append(col2).append(CLOSE_BRACKET);
            return this;
        }
        
        public ExpressionBuilder modulo(String col1, String col2) {
            expression.append(OPEN_BRACKET).append(col1).append(" % ").append(col2).append(CLOSE_BRACKET);
            return this;
        }
        
        /**
         * String Functions
         */
        public Expression concat(String... columns) {
            String cols = Arrays.stream(columns)
                .collect(Collectors.joining(COMMA_DELIMETER));
            return new Expression("CONCAT" + OPEN_BRACKET + cols + CLOSE_BRACKET);
        }
        
        public Expression substring(String column, int start, int length) {
            return new Expression("SUBSTRING" + OPEN_BRACKET + column + COMMA_DELIMETER + 
                start + COMMA_DELIMETER + length + CLOSE_BRACKET);
        }
        
        public Expression upper(String column) {
            return new Expression("UPPER" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression lower(String column) {
            return new Expression("LOWER" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression trim(String column) {
            return new Expression("TRIM" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression length(String column) {
            return new Expression("LENGTH" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression replace(String column, String search, String replace) {
            return new Expression("REPLACE" + OPEN_BRACKET + column + COMMA_DELIMETER + 
                SINGLE_QUOTE + search + SINGLE_QUOTE + COMMA_DELIMETER + 
                SINGLE_QUOTE + replace + SINGLE_QUOTE + CLOSE_BRACKET);
        }
        
        /**
         * Date Functions
         */
        public Expression currentDate() {
            return new Expression("CURRENT_DATE");
        }
        
        public Expression currentTimestamp() {
            return new Expression("CURRENT_TIMESTAMP");
        }
        
        public Expression now() {
            return new Expression("NOW()");
        }
        
        public Expression dateAdd(String column, int value, String unit) {
            return new Expression("DATE_ADD" + OPEN_BRACKET + column + COMMA_DELIMETER + 
                "INTERVAL " + value + " " + unit + CLOSE_BRACKET);
        }
        
        public Expression dateSub(String column, int value, String unit) {
            return new Expression("DATE_SUB" + OPEN_BRACKET + column + COMMA_DELIMETER + 
                "INTERVAL " + value + " " + unit + CLOSE_BRACKET);
        }
        
        public Expression dateDiff(String date1, String date2) {
            return new Expression("DATEDIFF" + OPEN_BRACKET + date1 + COMMA_DELIMETER + 
                date2 + CLOSE_BRACKET);
        }
        
        public Expression year(String column) {
            return new Expression("YEAR" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression month(String column) {
            return new Expression("MONTH" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression day(String column) {
            return new Expression("DAY" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression dateFormat(String column, String format) {
            return new Expression("DATE_FORMAT" + OPEN_BRACKET + column + COMMA_DELIMETER + 
                SINGLE_QUOTE + format + SINGLE_QUOTE + CLOSE_BRACKET);
        }
        
        /**
         * Conditional Expressions
         */
        public Expression caseWhen() {
            return new Expression("CASE");
        }
        
        public Expression ifNull(String column, String defaultValue) {
            return new Expression("IFNULL" + OPEN_BRACKET + column + COMMA_DELIMETER + 
                SINGLE_QUOTE + defaultValue + SINGLE_QUOTE + CLOSE_BRACKET);
        }
        
        public Expression coalesce(String... columns) {
            String cols = Arrays.stream(columns)
                .collect(Collectors.joining(COMMA_DELIMETER));
            return new Expression("COALESCE" + OPEN_BRACKET + cols + CLOSE_BRACKET);
        }
        
        public Expression nullIf(String expr1, String expr2) {
            return new Expression("NULLIF" + OPEN_BRACKET + expr1 + COMMA_DELIMETER + 
                expr2 + CLOSE_BRACKET);
        }
        
        /**
         * Type Conversion
         */
        public Expression cast(String column, String dataType) {
            return new Expression("CAST" + OPEN_BRACKET + column + " AS " + dataType + CLOSE_BRACKET);
        }
        
        public Expression convert(String column, String dataType) {
            return new Expression("CONVERT" + OPEN_BRACKET + column + COMMA_DELIMETER + 
                dataType + CLOSE_BRACKET);
        }
        
        /**
         * Numeric Functions
         */
        public Expression abs(String column) {
            return new Expression("ABS" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression ceil(String column) {
            return new Expression("CEIL" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression floor(String column) {
            return new Expression("FLOOR" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression round(String column, int precision) {
            return new Expression("ROUND" + OPEN_BRACKET + column + COMMA_DELIMETER + 
                precision + CLOSE_BRACKET);
        }
        
        public Expression power(String column, int exponent) {
            return new Expression("POWER" + OPEN_BRACKET + column + COMMA_DELIMETER + 
                exponent + CLOSE_BRACKET);
        }
        
        public Expression sqrt(String column) {
            return new Expression("SQRT" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        /**
         * Aggregate Expressions (for use in expressions)
         */
        public Expression sum(String column) {
            return new Expression("SUM" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression avg(String column) {
            return new Expression("AVG" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression count(String column) {
            return new Expression("COUNT" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression countDistinct(String column) {
            return new Expression("COUNT(DISTINCT " + column + ")");
        }
        
        public Expression min(String column) {
            return new Expression("MIN" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression max(String column) {
            return new Expression("MAX" + OPEN_BRACKET + column + CLOSE_BRACKET);
        }
        
        public Expression build() {
            return new Expression(expression.toString());
        }
    }
    
    /**
     * Static factory methods for common expressions
     */
    public static Expression raw(String sql) {
        return new Expression(sql);
    }
    
    public static Expression column(String columnName) {
        return new Expression(columnName);
    }
    
    public static Expression value(Object value) {
        if (value instanceof String) {
            return new Expression(SINGLE_QUOTE + value + SINGLE_QUOTE);
        }
        return new Expression(value.toString());
    }
    
    public static Expression literal(String value) {
        return new Expression(SINGLE_QUOTE + value + SINGLE_QUOTE);
    }
}
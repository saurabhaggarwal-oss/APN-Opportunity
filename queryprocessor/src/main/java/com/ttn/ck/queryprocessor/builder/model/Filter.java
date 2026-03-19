package com.ttn.ck.queryprocessor.builder.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.ttn.ck.queryprocessor.builder.utils.ApplicationConstants.*;

/**
 * Enhanced Filter with Operator enum support
 * Enables dynamic filter building for integration engineers
 */
@Data
@AllArgsConstructor
public class Filter {
    private StringBuilder filterExpression;

    public static FilterBuilder builder() {
        return new FilterBuilder();
    }

    public String toSql() {
        return filterExpression != null ? filterExpression.toString() : "";
    }

    public static class FilterBuilder {
        private final StringBuilder filterExpression = new StringBuilder();

        // ============================================================
        // OPERATOR-BASED FILTERING (For Integration Engineers)
        // ============================================================

        /**
         * Apply an operator with column and value
         * This is the KEY method for dynamic filter building
         * <p>
         * Example:
         * filter.apply("age", Operator.GREATER_THAN, 18)
         * filter.apply("status", Operator.IN, Arrays.asList("active", "pending"))
         */
        public FilterBuilder apply(String column, Operator operator, Object value) {
            String condition = operator.apply(column, value);
            this.filterExpression.append(condition).append(SPACE);
            return this;
        }

        /**
         * Apply operator without value (for IS NULL, IS NOT NULL)
         */
        public FilterBuilder apply(String column, Operator operator) {
            String condition = operator.apply(column, null);
            this.filterExpression.append(condition).append(SPACE);
            return this;
        }

        // ============================================================
        // LOGICAL OPERATORS
        // ============================================================

        public FilterBuilder and() {
            this.filterExpression.append(AND);
            return this;
        }

        public FilterBuilder or() {
            this.filterExpression.append(OR);
            return this;
        }

        public FilterBuilder openBracket() {
            this.filterExpression.append(OPEN_BRACKET);
            return this;
        }

        public FilterBuilder closeBracket() {
            this.filterExpression.append(CLOSE_BRACKET).append(SPACE);
            return this;
        }

        // ============================================================
        // TRADITIONAL METHODS (Backward compatibility)
        // ============================================================

        public FilterBuilder column(String columnName) {
            this.filterExpression.append(columnName).append(SPACE);
            return this;
        }

        public FilterBuilder eq(String value) {
            this.filterExpression.append(EQUAL).append(SINGLE_QUOTE)
                    .append(value).append(SINGLE_QUOTE).append(SPACE);
            return this;
        }

        public FilterBuilder eq(Integer value) {
            this.filterExpression.append(EQUAL).append(value.toString()).append(SPACE);
            return this;
        }

        public FilterBuilder greaterThan(Object value) {
            this.filterExpression.append(GREATER_THAN).append(value).append(SPACE);
            return this;
        }

        public FilterBuilder lessThan(Object value) {
            this.filterExpression.append(LESS_THAN).append(value).append(SPACE);
            return this;
        }

        public FilterBuilder like(String pattern) {
            this.filterExpression.append(LIKE).append(SINGLE_QUOTE)
                    .append(pattern).append(SINGLE_QUOTE).append(SPACE);
            return this;
        }

        public FilterBuilder in(Iterable<String> values) {
            List<String> valuesList = new ArrayList<>();
            for (String value : values) {
                valuesList.add(SINGLE_QUOTE + value + SINGLE_QUOTE);
            }
            this.filterExpression.append(IN).append(OPEN_BRACKET)
                    .append(String.join(COMMA_DELIMETER, valuesList))
                    .append(CLOSE_BRACKET).append(SPACE);
            return this;
        }

        public FilterBuilder between(Object value1, Object value2) {
            this.filterExpression.append(BETWEEN).append(SINGLE_QUOTE)
                    .append(value1).append(SINGLE_QUOTE).append(SPACE)
                    .append(AND).append(SINGLE_QUOTE)
                    .append(value2).append(SINGLE_QUOTE).append(SPACE);
            return this;
        }

        public FilterBuilder isNull() {
            this.filterExpression.append(IS_NULL).append(SPACE);
            return this;
        }

        public FilterBuilder isNotNull() {
            this.filterExpression.append(IS_NOT_NULL).append(SPACE);
            return this;
        }

        public FilterBuilder raw(String sqlExpression) {
            this.filterExpression.append(sqlExpression).append(SPACE);
            return this;
        }

        public FilterBuilder append(Filter filter) {
            if (Objects.nonNull(filter)) {
                this.filterExpression.append(filter.toSql());
            }
            return this;
        }

        public Filter build() {
            return new Filter(filterExpression);
        }

        public StringBuilder buildAsStringBuilder() {
            return filterExpression;
        }
    }
}
package com.ttn.ck.queryprocessor.builder.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.ttn.ck.queryprocessor.utils.ApplicationConstants.*;

/**
 * Represents HAVING clause for filtering on aggregate functions.
 * Separate from WHERE (which filters rows before grouping).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Having {

    private StringBuilder havingExpression;

    public static HavingBuilder builder() {
        return new HavingBuilder();
    }

    public String toSql() {
        return havingExpression != null && !havingExpression.isEmpty()
                ? " HAVING " + havingExpression
                : "";
    }

    public boolean isEmpty() {
        return havingExpression == null || havingExpression.isEmpty();
    }

    public static class HavingBuilder {

        private final StringBuilder havingExpression = new StringBuilder();

        /**
         * Generic apply when you already know the aggregate function and operator.
         * Example result: SUM(sales) > ?  or  COUNT(*) != ?
         */
        public HavingBuilder apply(String function, String column, String operatorSql) {
            if (function == null || function.isBlank()) {
                throw new IllegalArgumentException("function is required in HAVING");
            }
            if (column == null || column.isBlank()) {
                throw new IllegalArgumentException("column is required in HAVING");
            }
            if (operatorSql == null || operatorSql.isBlank()) {
                throw new IllegalArgumentException("operator is required in HAVING");
            }
            this.havingExpression
                    .append(function.toUpperCase())
                    .append("(")
                    .append(column)
                    .append(") ")
                    .append(operatorSql)
                    .append(SPACE)
                    .append("?")
                    .append(SPACE);
            return this;
        }

        /** Add raw condition string (already complete, including ? if any). */
        public HavingBuilder condition(String condition) {
            if (condition == null || condition.isBlank()) {
                return this;
            }
            this.havingExpression.append(condition).append(SPACE);
            return this;
        }

        /** Aggregate helpers (to be followed by comparison helpers). */
        public HavingBuilder count(String column) {
            this.havingExpression.append("COUNT(").append(column).append(")").append(SPACE);
            return this;
        }

        public HavingBuilder countStar() {
            this.havingExpression.append("COUNT(*)").append(SPACE);
            return this;
        }

        public HavingBuilder sum(String column) {
            this.havingExpression.append("SUM(").append(column).append(")").append(SPACE);
            return this;
        }

        public HavingBuilder avg(String column) {
            this.havingExpression.append("AVG(").append(column).append(")").append(SPACE);
            return this;
        }

        public HavingBuilder min(String column) {
            this.havingExpression.append("MIN(").append(column).append(")").append(SPACE);
            return this;
        }

        public HavingBuilder max(String column) {
            this.havingExpression.append("MAX(").append(column).append(")").append(SPACE);
            return this;
        }

        /** Comparison helpers – you still bind the value in the caller. */
        public HavingBuilder greaterThan() {
            this.havingExpression.append(">").append(SPACE).append("?").append(SPACE);
            return this;
        }

        public HavingBuilder greaterThanOrEquals() {
            this.havingExpression.append(">=").append(SPACE).append("?").append(SPACE);
            return this;
        }

        public HavingBuilder lessThan() {
            this.havingExpression.append("<").append(SPACE).append("?").append(SPACE);
            return this;
        }

        public HavingBuilder lessThanOrEquals() {
            this.havingExpression.append("<=").append(SPACE).append("?").append(SPACE);
            return this;
        }

        public HavingBuilder eq() {
            this.havingExpression.append("=").append(SPACE).append("?").append(SPACE);
            return this;
        }

        public HavingBuilder notEquals() {
            this.havingExpression.append("!=").append(SPACE).append("?").append(SPACE);
            return this;
        }

        public HavingBuilder between() {
            this.havingExpression
                    .append("BETWEEN")
                    .append(SPACE)
                    .append("?")
                    .append(SPACE)
                    .append("AND")
                    .append(SPACE)
                    .append("?")
                    .append(SPACE);
            return this;
        }

        /** Logical operators and grouping. */
        public HavingBuilder and() {
            this.havingExpression.append("AND").append(SPACE);
            return this;
        }

        public HavingBuilder or() {
            this.havingExpression.append("OR").append(SPACE);
            return this;
        }

        public HavingBuilder openBracket() {
            this.havingExpression.append(OPEN_BRACKET);
            return this;
        }

        public HavingBuilder closeBracket() {
            this.havingExpression.append(CLOSE_BRACKET).append(SPACE);
            return this;
        }

        public Having build() {
            return new Having(havingExpression);
        }
    }
}

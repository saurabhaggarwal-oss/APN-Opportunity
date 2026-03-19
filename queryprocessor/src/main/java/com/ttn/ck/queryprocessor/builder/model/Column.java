package com.ttn.ck.queryprocessor.builder.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.ttn.ck.queryprocessor.builder.utils.ApplicationConstants.*;

/**
 * MATURE Column class - Universal container for all SELECT items
 * Handles: simple columns, expressions, window functions, aggregates, CASE statements, subqueries
 * <p>
 * Philosophy: Everything that appears in SELECT is a Column with an optional alias
 */
@Data
@AllArgsConstructor
public class Column {
    private String expression;
    private String alias;

    public Column(String expression) {
        this.expression = expression;
        this.alias = null;
    }

    public static ColumnBuilder builder() {
        return new ColumnBuilder();
    }

    public String toSql() {
        if (StringUtils.hasText(alias)) {
            return expression + AS + alias;
        }
        return expression;
    }

    public static class ColumnBuilder {
        private String expression;
        private String alias;

        // ============================================================
        // SIMPLE COLUMNS
        // ============================================================

        /**
         * Simple column reference
         */
        public ColumnBuilder name(String columnName) {
            this.expression = columnName;
            return this;
        }

        /**
         * ALL columns (*)
         */
        public ColumnBuilder all() {
            this.expression = "*";
            return this;
        }

        // ============================================================
        // FROM EXPRESSION (Universal method)
        // ============================================================

        /**
         * Create column from Expression object
         */
        public ColumnBuilder fromExpression(Expression expr) {
            this.expression = expr.toSql();
            return this;
        }

        // New: accept AggregateFunction
        public ColumnBuilder fromAggregate(AggregateFunction agg) {
            this.expression = agg.toSqlWithoutAlias();
            if (StringUtils.hasText(agg.getAlias())) {
                this.alias = agg.getAlias();
            }
            return this;
        }
        // ============================================================
        // FROM WINDOW FUNCTION (Universal method)
        // ============================================================

        /**
         * Create column from WindowFunction object
         */
        public ColumnBuilder fromWindowFunction(WindowFunction winFunc) {
            this.expression = winFunc.toSqlWithoutAlias();
            // Window function may have its own alias
            if (StringUtils.hasText(winFunc.getAlias())) {
                this.alias = winFunc.getAlias();
            }
            return this;
        }

        // ============================================================
        // FROM CASE EXPRESSION (Universal method)
        // ============================================================

        /**
         * Create column from CaseExpression object
         */
        public ColumnBuilder fromCaseExpression(CaseExpression caseExpr) {
            this.expression = caseExpr.toSqlWithoutAlias();
            // Case expression may have its own alias
            if (StringUtils.hasText(caseExpr.getAlias())) {
                this.alias = caseExpr.getAlias();
            }
            return this;
        }

        // ============================================================
        // FROM SUBQUERY (Universal method)
        // ============================================================

        /**
         * Create column from SubQuery (scalar subquery in SELECT)
         */
        public ColumnBuilder fromSubQuery(SubQuery subQuery) {
            this.expression = OPEN_BRACKET + subQuery.toSqlWithoutParentheses() + CLOSE_BRACKET;
            if (StringUtils.hasText(subQuery.getAlias())) {
                this.alias = subQuery.getAlias();
            }
            return this;
        }

        // ============================================================
        // AGGREGATE FUNCTIONS (Convenience methods)
        // ============================================================

        /**
         * SUM aggregate function
         */
        public ColumnBuilder sum(String columnName) {
            this.expression = SUM + OPEN_BRACKET + columnName + CLOSE_BRACKET;
            return this;
        }

        /**
         * MIN aggregate function
         */
        public ColumnBuilder min(String columnName) {
            this.expression = MIN + OPEN_BRACKET + columnName + CLOSE_BRACKET;
            return this;
        }

        /**
         * MAX aggregate function
         */
        public ColumnBuilder max(String columnName) {
            this.expression = MAX + OPEN_BRACKET + columnName + CLOSE_BRACKET;
            return this;
        }

        /**
         * COUNT aggregate function
         */
        public ColumnBuilder count(String columnName) {
            this.expression = COUNT + OPEN_BRACKET + columnName + CLOSE_BRACKET;
            return this;
        }

        /**
         * COUNT DISTINCT
         */
        public ColumnBuilder countDistinct(String columnName) {
            this.expression = "COUNT(DISTINCT " + columnName + ")";
            return this;
        }

        /**
         * AVG aggregate function
         */
        public ColumnBuilder avg(String columnName) {
            this.expression = AVG + OPEN_BRACKET + columnName + CLOSE_BRACKET;
            return this;
        }

        /**
         * ROUND function
         */
        public ColumnBuilder round(String columnName, int precision) {
            this.expression = ROUND + OPEN_BRACKET + columnName + COMMA_DELIMETER +
                    precision + CLOSE_BRACKET;
            return this;
        }

        /**
         * COALESCE function
         */
        public ColumnBuilder coalesce(String columnName, List<String> defaultValues) {
            String defaults = defaultValues.stream()
                    .map(e -> SINGLE_QUOTE + e + SINGLE_QUOTE)
                    .collect(Collectors.joining(COMMA_DELIMETER));

            this.expression = "COALESCE" + OPEN_BRACKET + columnName + COMMA_DELIMETER +
                    defaults + CLOSE_BRACKET;
            return this;
        }

        // ============================================================
        // RAW SQL
        // ============================================================

        /**
         * Raw SQL expression
         */
        public ColumnBuilder raw(String sqlExpression) {
            this.expression = sqlExpression;
            return this;
        }

        // ============================================================
        // ALIAS
        // ============================================================

        /**
         * Set alias for the column
         */
        public ColumnBuilder as(String alias) {
            this.alias = alias;
            return this;
        }

        public Column build() {
            return new Column(expression, alias);
        }
    }

    // ============================================================
    // STATIC FACTORY METHODS (Convenience)
    // ============================================================

    /**
     * Create column from simple name
     */
    public static Column of(String columnName) {
        return new Column(columnName);
    }

    /**
     * Create column from simple name with alias
     */
    public static Column of(String columnName, String alias) {
        return new Column(columnName, alias);
    }

    /**
     * Create column from Expression
     */
    public static Column of(Expression expression) {
        return Column.builder()
                .fromExpression(expression)
                .build();
    }

    /**
     * Create column from Expression with alias
     */
    public static Column of(Expression expression, String alias) {
        return Column.builder()
                .fromExpression(expression)
                .as(alias)
                .build();
    }

    /**
     * Create column from WindowFunction
     */
    public static Column of(WindowFunction windowFunction) {
        return Column.builder()
                .fromWindowFunction(windowFunction)
                .build();
    }

    /**
     * Create column from CaseExpression
     */
    public static Column of(CaseExpression caseExpression) {
        return Column.builder()
                .fromCaseExpression(caseExpression)
                .build();
    }

    /**
     * Create column from SubQuery
     */
    public static Column of(SubQuery subQuery) {
        return Column.builder()
                .fromSubQuery(subQuery)
                .build();
    }

    public static Column of(AggregateFunction agg) {
        return Column.builder().fromAggregate(agg).build();
    }

    /**
     * Create ALL columns (*)
     */
    public static Column all() {
        return new Column("*");
    }
}
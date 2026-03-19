package com.ttn.ck.queryprocessor.builder.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static com.ttn.ck.queryprocessor.utils.ApplicationConstants.AS;
import static com.ttn.ck.queryprocessor.utils.ApplicationConstants.SINGLE_QUOTE;

/**
 * CASE Expression - Returns CASE SQL that can be wrapped in Column
 */
@Data
@AllArgsConstructor
public class CaseExpression {
    private List<WhenClause> whenClauses;
    private String elseValue;
    private String alias;

    public CaseExpression() {
        this.whenClauses = new ArrayList<>();
    }

    public static CaseBuilder builder() {
        return new CaseBuilder();
    }

    /**
     * Get SQL with alias (for direct use)
     */
    public String toSql() {
        StringBuilder sql = new StringBuilder();
        sql.append(toSqlWithoutAlias());

        if (alias != null && !alias.isEmpty()) {
            sql.append(AS).append(alias);
        }

        return sql.toString();
    }

    /**
     * Get SQL without alias (for wrapping in Column)
     */
    public String toSqlWithoutAlias() {
        StringBuilder sql = new StringBuilder("CASE");

        for (WhenClause when : whenClauses) {
            sql.append(" WHEN ").append(when.getCondition())
                    .append(" THEN ").append(when.getResult());
        }

        if (elseValue != null) {
            sql.append(" ELSE ").append(elseValue);
        }

        sql.append(" END");
        return sql.toString();
    }

    @Data
    @AllArgsConstructor
    public static class WhenClause {
        private String condition;
        private String result;
    }

    public static class CaseBuilder {
        private final List<WhenClause> whenClauses = new ArrayList<>();
        private String elseValue;
        private String alias;

        public CaseBuilder when(String condition, String result) {
            this.whenClauses.add(new WhenClause(condition, result));
            return this;
        }

        public CaseBuilder whenThen(String condition, String result) {
            this.whenClauses.add(new WhenClause(condition, SINGLE_QUOTE + result + SINGLE_QUOTE));
            return this;
        }

        public CaseBuilder whenThen(String condition, Number result) {
            this.whenClauses.add(new WhenClause(condition, result.toString()));
            return this;
        }

        public CaseBuilder when(Filter filter, String result) {
            this.whenClauses.add(new WhenClause(filter.toSql().trim(), result));
            return this;
        }

        public CaseBuilder elseValue(String value) {
            this.elseValue = SINGLE_QUOTE + value + SINGLE_QUOTE;
            return this;
        }

        public CaseBuilder elseValue(Number value) {
            this.elseValue = value.toString();
            return this;
        }

        public CaseBuilder elseRaw(String expression) {
            this.elseValue = expression;
            return this;
        }

        public CaseBuilder as(String alias) {
            this.alias = alias;
            return this;
        }

        public CaseExpression build() {
            return new CaseExpression(whenClauses, elseValue, alias);
        }
    }
}
package com.ttn.ck.queryprocessor.service;

import com.ttn.ck.queryprocessor.utils.ApplicationConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.ttn.ck.queryprocessor.utils.ApplicationConstants.*;

@Data
@AllArgsConstructor
public class QueryBuilder {

    private String tableName;
    private List<String> columns;
    private StringBuilder filter;
    private List<String> groupBy;
    private List<String> orderBy;
    private String placeHolders;
    private String setClause;

    public static QueryStringBuilder builder() {
        return new QueryStringBuilder();
    }

    public static class QueryStringBuilder {
        private String tableName;
        private final List<String> columns  = new ArrayList<>();
        private StringBuilder filter;
        private final List<String> groupBy = new ArrayList<>();
        private final List<String> orderBy = new ArrayList<>();
        private String placeHolders;
        private String setClause;

        public QueryStringBuilder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public QueryStringBuilder columns(Collection<String> columns) {
            this.columns.addAll(columns);
            return this;
        }

        public QueryStringBuilder column(String columnName, String alias) {
            this.columns.add(columnName.concat(AS).concat(StringUtils.hasText(alias) ? alias : columnName));
            return this;
        }

        public QueryStringBuilder coalesceColumn(String columnName, List<String> coalesceList, String alias) {
            this.columns.add("COALESCE"
                    .concat(OPEN_BRACKET)
                    .concat(columnName)
                    .concat(COMMA_DELIMETER)
                    .concat(coalesceList.stream()
                            .map(e -> SINGLE_QUOTE + e + SINGLE_QUOTE)
                            .collect(Collectors.joining(COMMA_DELIMETER)))
                    .concat(CLOSE_BRACKET)
                    .concat(AS)
                    .concat("\"" + alias.toUpperCase() + "\""));
            return this;
        }

        public QueryStringBuilder column(String columnStr) {
            this.columns.add(columnStr);
            return this;
        }

        public QueryStringBuilder sum(String columnName, String alias) {
            this.columns.add(
                            SUM
                            .concat(OPEN_BRACKET)
                            .concat(columnName)
                            .concat(CLOSE_BRACKET)
                            .concat(AS)
                            .concat(StringUtils.hasText(alias) ? alias : columnName)
            );
            return this;
        }

        public QueryStringBuilder sum(String columnName) {
            this.columns.add(
                            SUM
                            .concat(OPEN_BRACKET)
                            .concat(columnName)
                            .concat(CLOSE_BRACKET)
            );
            return this;
        }

        public QueryStringBuilder filter(StringBuilder filter) {
            this.filter = filter;
            return this;
        }

        public QueryStringBuilder groupBy(String groupBy) {
            this.groupBy.add(groupBy);
            return this;
        }

        public QueryStringBuilder groupBy(List<String> groupBy) {
            this.groupBy.addAll(groupBy);
            return this;
        }

        public QueryStringBuilder groupByOrder(String orderString){
            this.groupBy.addAll(Arrays.stream(orderString.split(COMMA_DELIMETER)).toList());
            return this;
        }

        public StringBuilder build() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(SELECT)
                    .append(String.join(COMMA_DELIMETER, columns))
                    .append(FROM)
                    .append(tableName);
            addFilter(filter, stringBuilder);
            addGroupBy(groupBy, stringBuilder);
            addOrderBy(orderBy, stringBuilder);
            return stringBuilder;
        }

        public StringBuilder buildInsert() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(INSERT_INTO)
                    .append(tableName)
                    .append(OPEN_BRACKET)
                    .append(String.join(COMMA_DELIMETER, columns))
                    .append(CLOSE_BRACKET)
                    .append(VALUES)
                    .append(OPEN_BRACKET)
                    .append(placeHolders)
                    .append(CLOSE_BRACKET);
            return stringBuilder;
        }

        public StringBuilder buildUpdate() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(UPDATE)
                    .append(tableName)
                    .append(SET)
                    .append(setClause);
            addFilter(filter, stringBuilder);
            return stringBuilder;
        }

        public QueryStringBuilder placeHolders(Collection<String> placeHolders) {
            this.placeHolders = String.join(COMMA_DELIMETER, placeHolders);
            return this;
        }

        public QueryStringBuilder setClause(String setClause) {
            this.setClause = setClause;
            return this;
        }

        public void addFilter(StringBuilder filter, StringBuilder stringBuilder) {
            if (Objects.nonNull(filter)) {
                stringBuilder.append(WHERE).append(filter);
            }
        }

        public void addGroupBy(List<String> groupBy, StringBuilder stringBuilder) {
            if (!groupBy.isEmpty()) {
                stringBuilder.append(GROUP_BY).append(String.join(COMMA_DELIMETER, groupBy));
            }
        }

        public void addOrderBy(List<String> orderBy, StringBuilder stringBuilder) {
            if (!orderBy.isEmpty()) {
                stringBuilder.append(ORDER_BY).append(String.join(COMMA_DELIMETER, orderBy));
            }
        }

        public QueryStringBuilder orderBy(String orderBy) {
            this.orderBy.add(orderBy);
            return this;
        }
        public QueryStringBuilder minColumn(String columnName) {
            this.columns.add(
                    ApplicationConstants.MIN
                            .concat(OPEN_BRACKET)
                            .concat(columnName)
                            .concat(CLOSE_BRACKET)
            );
            return this;
        }

        public QueryStringBuilder round(String columnName, int digit, String alias){
            this.columns.add(
                    ApplicationConstants.ROUND
                            .concat(OPEN_BRACKET)
                            .concat(columnName)
                            .concat(COMMA_DELIMETER)
                            .concat(String.valueOf(digit))
                            .concat(CLOSE_BRACKET)
                            .concat(SPACE)
                            .concat(AS)
                            .concat(StringUtils.hasText(alias) ? alias : columnName)
            );
            return this;
        }

    }

}

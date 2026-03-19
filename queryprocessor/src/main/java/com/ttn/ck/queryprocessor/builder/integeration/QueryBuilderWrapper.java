package com.ttn.ck.queryprocessor.builder.integeration;

import com.ttn.ck.queryprocessor.builder.dto.JoinDTO;
import com.ttn.ck.queryprocessor.builder.dto.OrderByDTO;
import com.ttn.ck.queryprocessor.builder.dto.QueryComponentDTO;
import com.ttn.ck.queryprocessor.builder.model.*;
import com.ttn.ck.queryprocessor.builder.secure.SecureFilter;
import com.ttn.ck.queryprocessor.builder.secure.SqlValidationEngine;
import com.ttn.ck.queryprocessor.builder.service.QueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

// Returns built SQL and parameter list as a result pair.
public class QueryBuilderWrapper {

    private final SelectItemColumnMapper selectMapper = new SelectItemColumnMapper();

    public record QueryBuildResult(String sql, List<Object> params) {
    }


    public QueryBuildResult build(QueryComponentDTO qc, SqlValidationEngine ve) {
        return build(qc, null, ve);
    }

    public QueryBuildResult build(QueryComponentDTO qc, CTE cte, SqlValidationEngine ve) {
        List<Object> params = new ArrayList<>();

        // SELECT
        List<Column> selectCols = selectMapper.toColumns(qc.getSelect());

        // JOIN
        Join join = buildJoin(qc.getJoins());

        // WHERE
        SecureFilter where = null;
        if (qc.getFilters() != null && !qc.getFilters().isEmpty()) {
            where = FilterWrapperDTOMapper.mapToSecureFilter(qc.getFilters());
            params.addAll(where.getParams());
        }

        // GROUP BY
        GroupBy groupBy = new GroupBy();
        if (qc.getGroupBy() != null && !qc.getGroupBy().isEmpty()) {
            GroupBy.GroupByBuilder gb = GroupBy.builder();
            qc.getGroupBy().forEach(gb::column);
            groupBy = gb.build();
        }

        // HAVING
        Having having = new Having();
        if (qc.getHaving() != null && !qc.getHaving().isEmpty()) {
            having = HavingWrapperDTOMapper.mapToHaving(qc.getHaving(), params);
        }
        // ORDER BY
        OrderBy orderBy = new OrderBy();
        if (qc.getOrderBy() != null && !qc.getOrderBy().isEmpty()) {
            OrderBy.OrderByBuilder ob = OrderBy.builder();
            for (OrderByDTO o : qc.getOrderBy()) {
                String dir = (o.getDirection() == null) ? "ASC" : o.getDirection().toUpperCase(Locale.ROOT);
                if ("DESC".equals(dir)) ob.desc(o.getColumn());
                else ob.asc(o.getColumn());
            }
            orderBy = ob.build();
        }


        // Table reference
        String tableRef = qualify(qc.getSchema(), qc.getTable());
        QueryBuilder.Builder qb = QueryBuilder.builder();
        if (Objects.nonNull(cte)) {
            qb.with(cte);
        }
        String sql = qb
                .from(tableRef, qc.getAlias())
                .select(selectCols)
                .joins(join)
                .where(where)
                .groupBy(groupBy)
                .having(having)
                .orderBy(orderBy)
                .limit(qc.getLimit())
                .offset(qc.getOffset())
                .build();

        return new QueryBuildResult(sql, params);
    }

    private Join buildJoin(List<JoinDTO> joinDTOs) {
        if (joinDTOs == null || joinDTOs.isEmpty()) return null;
        Join.JoinBuilder jb = Join.builder();
        for (JoinDTO j : joinDTOs) {
            String type = (j.getType() == null) ? "INNER" : j.getType().toUpperCase(Locale.ROOT);
            switch (type) {
                case "LEFT" -> jb.leftJoin(j.getTable(), j.getAlias(), j.getOn());
                case "RIGHT" -> jb.rightJoin(j.getTable(), j.getAlias(), j.getOn());
                case "FULL" -> jb.fullJoin(j.getTable(), j.getAlias(), j.getOn());
                case "CROSS" -> jb.crossJoin(j.getTable(), j.getAlias());
                case "INNER" -> jb.innerJoin(j.getTable(), j.getAlias(), j.getOn());
            }
        }
        return jb.build();
    }

    private static String qualify(String schema, String table) {
        return (schema == null || schema.isBlank()) ? table : schema + "." + table;
    }

    private static String toSqlOperator(String op, boolean not) {
        return switch (op.toUpperCase(Locale.ROOT)) {
            case "EQUALS" -> not ? "!=" : "=";
            case "NOT_EQUALS" -> "!=";
            case "GREATER_THAN" -> ">";
            case "GREATER_THAN_OR_EQUAL" -> ">=";
            case "LESS_THAN" -> "<";
            case "LESS_THAN_OR_EQUAL" -> "<=";
            default -> throw new IllegalArgumentException("Unsupported HAVING operator: " + op);
        };
    }
}

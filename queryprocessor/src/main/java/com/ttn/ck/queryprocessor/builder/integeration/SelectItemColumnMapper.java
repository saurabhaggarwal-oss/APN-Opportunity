package com.ttn.ck.queryprocessor.builder.integeration;

import com.ttn.ck.queryprocessor.builder.dto.*;
import com.ttn.ck.queryprocessor.builder.model.*;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SelectItemColumnMapper {

    public List<Column> toColumns(List<SelectItemDTO> items) {
        if (items == null || items.isEmpty()) return List.of(Column.all());
        List<Column> cols = new ArrayList<>(items.size());
        for (SelectItemDTO s : items) {
            cols.add(toColumn(s));
        }
        return cols;
    }

    public Column toColumn(SelectItemDTO s) {
        if (s == null || s.getKind() == null) {
            throw new IllegalArgumentException("Select item is null or missing kind");
        }
        return switch (s.getKind()) {
            case COLUMN -> buildColumnName(s);
            case RAW_EXPRESSION -> buildRawExpression(s);
            case CASE -> buildCase(s);
            case AGGREGATE -> buildAggregate(s);
            case WINDOW -> buildWindow(s);
        };
    }

    private Column buildColumnName(SelectItemDTO s) {
        String name = s.getName();
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("COLUMN kind requires non-empty name");
        }
        Column col = Column.builder().name(name).build();
        return withAliasOverride(col, s.getAlias());
    }

    private Column buildRawExpression(SelectItemDTO s) {
        String expr = s.getExpression();
        if (!StringUtils.hasText(expr)) {
            throw new IllegalArgumentException("RAW_EXPRESSION kind requires non-empty expression");
        }
        Column col = Column.builder()
                .fromExpression(Expression.builder().raw(expr))
                .build();
        return withAliasOverride(col, s.getAlias());
    }

    private Column buildCase(SelectItemDTO s) {
        CaseExpressionDTO cd = s.getCaseExpr();
        if (cd == null) {
            throw new IllegalArgumentException("CASE kind requires caseExpr");
        }
        CaseExpression.CaseBuilder cb = CaseExpression.builder();
        if (cd.getWhens() != null) {
            for (WhenClauseDTO w : cd.getWhens()) {
                cb.when(w.getCondition(), w.getResult());
            }
        }
        if (StringUtils.hasText(cd.getElseResult())) {
            cb.elseRaw(cd.getElseResult());
        }
        if (StringUtils.hasText(cd.getAlias())) {
            cb.as(cd.getAlias());
        }
        Column col = Column.builder().fromCaseExpression(cb.build()).build();
        return withAliasOverride(col, s.getAlias());
    }

    private Column buildAggregate(SelectItemDTO s) {
        AggregateDTO a = s.getAggregate();
        if (a == null || !StringUtils.hasText(a.getFn())) {
            throw new IllegalArgumentException("AGGREGATE kind requires aggregate.fn");
        }
        AggregateFunction.Builder ab = AggregateFunction.builder();
        String fn = a.getFn().trim().toUpperCase(Locale.ROOT);
        List<String> args = (a.getArgs() == null) ? List.of() : a.getArgs();

        switch (fn) {
            case "SUM" -> ab.sum(requireArg(args));
            case "AVG" -> ab.avg(requireArg(args));
            case "MIN" -> ab.min(requireArg(args));
            case "MAX" -> ab.max(requireArg(args));
            case "COUNT" -> {
                if (!args.isEmpty() && "*".equals(args.get(0))) ab.countStar();
                else ab.count(requireArg(args));
            }
            case "STRING_AGG" -> ab.stringAgg(requireArg(args), a.getSeparator());
            case "ARRAY_AGG" -> ab.arrayAgg(requireArg(args));
            case "JSON_AGG" -> ab.jsonAgg(requireArg(args));
            case "STDDEV" -> ab.stddev(requireArg(args));
            case "VARIANCE" -> ab.variance(requireArg(args));
            case "PERCENTILE_DISC" -> {
                double p = requirePercentile(args);
                ab.percentileDisc(p);
            }
            case "PERCENTILE_CONT" -> {
                double p = requirePercentile(args);
                ab.percentileCont(p);
            }
            default -> throw new IllegalArgumentException("Unsupported aggregate fn: " + fn);
        }

        if (a.isDistinct()) ab.distinct();
        if (a.getOrderByWithin() != null) a.getOrderByWithin().forEach(ab::orderByWithin);
        if (StringUtils.hasText(a.getFilterWhere())) ab.filterWhere(a.getFilterWhere());
        if (StringUtils.hasText(a.getWithinGroupOrderBy())) ab.withinGroupOrderBy(a.getWithinGroupOrderBy());
        if (a.getOverPartitionBy() != null && !a.getOverPartitionBy().isEmpty()) {
            ab.overPartitionBy(a.getOverPartitionBy().toArray(new String[0]));
        }
        if (a.getOverOrderBy() != null && !a.getOverOrderBy().isEmpty()) {
            ab.overOrderBy(a.getOverOrderBy().toArray(new String[0]));
        }
        if (StringUtils.hasText(a.getFrameClause())) ab.frame(a.getFrameClause());
        if (StringUtils.hasText(a.getAlias())) ab.as(a.getAlias());

        AggregateFunction agg = ab.build();
        Column col = Column.builder().fromAggregate(agg).build();
        return withAliasOverride(col, s.getAlias());
    }

    private Column buildWindow(SelectItemDTO s) {
        WindowFunctionDTO w = s.getWindow();
        if (w == null || !StringUtils.hasText(w.getFunctionName())) {
            throw new IllegalArgumentException("WINDOW kind requires window.functionName");
        }
        WindowFunction wf = new WindowFunction(
                w.getFunctionName(),
                emptyToNull(w.getPartitionBy()),
                emptyToNull(w.getOrderBy()),
                emptyToNull(w.getFrameClause()),
                emptyToNull(w.getAlias())
        );
        Column col = Column.builder().fromWindowFunction(wf).build();
        return withAliasOverride(col, s.getAlias());
    }

    private static String requireArg(List<String> args) {
        if (args == null || args.isEmpty() || !StringUtils.hasText(args.get(0))) {
            throw new IllegalArgumentException("Aggregate requires a non-empty argument");
        }
        return args.get(0);
    }

    private static double requirePercentile(List<String> args) {
        if (args == null || args.isEmpty() || !StringUtils.hasText(args.get(0))) {
            throw new IllegalArgumentException("Percentile aggregate requires percentile argument");
        }
        double p = Double.parseDouble(args.get(0));
        if (p < 0.0 || p > 1.0) throw new IllegalArgumentException("Percentile must be between 0.0 and 1.0");
        return p;
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static Column withAliasOverride(Column col, String aliasOverride) {
        if (StringUtils.hasText(aliasOverride)) {
            return new Column(col.getExpression(), aliasOverride);
        }
        return col;
    }
}

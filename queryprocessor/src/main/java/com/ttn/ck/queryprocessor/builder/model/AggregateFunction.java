package com.ttn.ck.queryprocessor.builder.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ttn.ck.queryprocessor.builder.utils.ApplicationConstants.*;

@Data
@AllArgsConstructor
public class AggregateFunction {
    private String functionSql;
    private String alias;

    public static Builder builder() { return new Builder(); }

    public String toSql() {
        String sql = toSqlWithoutAlias();
        if (StringUtils.hasText(alias)) {
            sql += AS + alias;
        }
        return sql;
    }

    public String toSqlWithoutAlias() {
        return functionSql;
    }

    public static class Builder {
        // Core
        private String name;                    // e.g., SUM, COUNT, STRING_AGG
        private final List<String> args = new ArrayList<>();
        private boolean distinct;
        private String alias;

        // Optional aggregate decorations
        private final List<String> orderByWithin = new ArrayList<>(); // ORDER BY inside aggregate (e.g., string_agg)
        private String separator;               // for STRING_AGG(expr, 'sep')
        private String withinGroupOrderBy;      // for ordered-set aggregates: WITHIN GROUP (ORDER BY ...)
        private String filterWhere;             // FILTER (WHERE ...)

        // Optional OVER clause (windowed aggregate)
        private final List<String> overPartitionBy = new ArrayList<>();
        private final List<String> overOrderBy = new ArrayList<>();
        private String frameClause;             // e.g., ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW

        // ---- Builders for common aggregates ----
        public Builder sum(String expr) { this.name = "SUM"; return argument(expr); }
        public Builder avg(String expr) { this.name = "AVG"; return argument(expr); }
        public Builder min(String expr) { this.name = "MIN"; return argument(expr); }
        public Builder max(String expr) { this.name = "MAX"; return argument(expr); }
        public Builder count(String expr) { this.name = "COUNT"; return argument(expr); }
        public Builder countStar() { this.name = "COUNT"; this.args.clear(); this.args.add("*"); return this; }
        public Builder countDistinct(String expr) { this.name = "COUNT"; this.distinct = true; return argument(expr); }

        // Advanced/common variants
        public Builder stddev(String expr) { this.name = "STDDEV"; return argument(expr); }
        public Builder variance(String expr) { this.name = "VARIANCE"; return argument(expr); }
        public Builder arrayAgg(String expr) { this.name = "ARRAY_AGG"; return argument(expr); }
        public Builder jsonAgg(String expr) { this.name = "JSON_AGG"; return argument(expr); }
        public Builder stringAgg(String expr, String sep) { this.name = "STRING_AGG"; this.separator = sep; return argument(expr); }

        // Ordered-set aggregates (PostgreSQL style)
        public Builder percentileDisc(double p) { this.name = "PERCENTILE_DISC"; this.args.clear(); this.args.add(Double.toString(p)); return this; }
        public Builder percentileCont(double p) { this.name = "PERCENTILE_CONT"; this.args.clear(); this.args.add(Double.toString(p)); return this; }
        public Builder withinGroupOrderBy(String orderBySql) { this.withinGroupOrderBy = orderBySql; return this; }

        // Argument helpers
        public Builder argument(String raw) { this.args.add(raw); return this; }
        public Builder argument(Expression expr) { return argument(expr.toSql()); }
        public Builder argument(CaseExpression caseExpr) { return argument(caseExpr.toSqlWithoutAlias()); }

        // Distinct toggle
        public Builder distinct() { this.distinct = true; return this; }

        // ORDER BY within aggregate (e.g., STRING_AGG(expr ORDER BY col))
        public Builder orderByWithin(String orderBySql) { this.orderByWithin.add(orderBySql); return this; }

        // FILTER (WHERE ...)
        public Builder filterWhere(String whereSql) { this.filterWhere = whereSql; return this; }

        // OVER clause
        public Builder overPartitionBy(String... cols) { this.overPartitionBy.addAll(Arrays.asList(cols)); return this; }
        public Builder overOrderBy(String... cols) { this.overOrderBy.addAll(Arrays.asList(cols)); return this; }
        public Builder frame(String frame) { this.frameClause = frame; return this; }

        // Alias
        public Builder as(String alias) { this.alias = alias; return this; }

        public AggregateFunction build() {
            StringBuilder sb = new StringBuilder();

            // Name and arguments
            sb.append(name).append(OPEN_BRACKET);
            if ("STRING_AGG".equalsIgnoreCase(name)) {
                // STRING_AGG(expr, 'sep' [ORDER BY ...])
                String expr = args.isEmpty() ? "*" : args.get(0);
                sb.append(expr);
                if (separator != null) {
                    sb.append(COMMA_DELIMETER).append(SINGLE_QUOTE).append(separator).append(SINGLE_QUOTE);
                }
                if (!orderByWithin.isEmpty()) {
                    sb.append(" ORDER BY ").append(String.join(COMMA_DELIMETER, orderByWithin));
                }
                sb.append(CLOSE_BRACKET);
            } else if ("PERCENTILE_DISC".equalsIgnoreCase(name) || "PERCENTILE_CONT".equalsIgnoreCase(name)) {
                // percentile_disc(p) WITHIN GROUP (ORDER BY expr)
                sb.append(String.join(COMMA_DELIMETER, args)).append(CLOSE_BRACKET);
                if (StringUtils.hasText(withinGroupOrderBy)) {
                    sb.append(" WITHIN GROUP (ORDER BY ").append(withinGroupOrderBy).append(")");
                }
            } else {
                // Standard aggregates: SUM, AVG, COUNT, etc.
                if (distinct) sb.append("DISTINCT ");
                sb.append(String.join(COMMA_DELIMETER, args)).append(CLOSE_BRACKET);
                if (!orderByWithin.isEmpty()) {
                    // Optional ORDER BY within aggregate (vendor-specific)
                    sb.append(" ORDER BY ").append(String.join(COMMA_DELIMETER, orderByWithin));
                }
            }

            // FILTER (WHERE ...)
            if (StringUtils.hasText(filterWhere)) {
                sb.append(" FILTER (WHERE ").append(filterWhere).append(CLOSE_BRACKET);
            }

            // OVER ( ... )
            boolean useOver = !overPartitionBy.isEmpty() || !overOrderBy.isEmpty() || StringUtils.hasText(frameClause);
            if (useOver) {
                sb.append(" OVER (");
                boolean needSpace = false;
                if (!overPartitionBy.isEmpty()) {
                    sb.append("PARTITION BY ").append(String.join(COMMA_DELIMETER, overPartitionBy));
                    needSpace = true;
                }
                if (!overOrderBy.isEmpty()) {
                    if (needSpace) sb.append(SPACE);
                    sb.append("ORDER BY ").append(String.join(COMMA_DELIMETER, overOrderBy));
                    needSpace = true;
                }
                if (StringUtils.hasText(frameClause)) {
                    if (needSpace) sb.append(SPACE);
                    sb.append(frameClause);
                }
                sb.append(CLOSE_BRACKET);
            }

            return new AggregateFunction(sb.toString(), alias);
        }
    }

    public static AggregateFunction of(String functionSql) {
        return new AggregateFunction(functionSql, null);
    }
}

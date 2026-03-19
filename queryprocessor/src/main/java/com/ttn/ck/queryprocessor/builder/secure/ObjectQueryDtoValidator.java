package com.ttn.ck.queryprocessor.builder.secure;


import com.ttn.ck.queryprocessor.builder.dto.*;
import com.ttn.ck.queryprocessor.builder.model.Operator;

import java.util.*;
import java.util.regex.Pattern;

public final class ObjectQueryDtoValidator {

    private final SqlValidationEngine ve;
    private QueryComponentDTO qc;

    private static final Pattern NUMERIC_LITERAL =
            Pattern.compile("^-?\\d+(\\.\\d+)?$", Pattern.DOTALL);

    private static final Pattern STRING_LITERAL =
            Pattern.compile("^'(?:[^']|''|\\\\')*'$", Pattern.DOTALL);

    private static final Set<String> DIRECTION = Set.of("ASC", "DESC");

    public ObjectQueryDtoValidator(SqlValidationEngine ve) {
        this.ve = ve;
    }

    // ENTRY POINT
    public void validateObjectQueryDTO(ObjectQueryDTO dto) {
        qc = requireNonNull(dto.getQueryComponents(), "queryComponents");
        validateBaseSchemaAndTable(qc);

        Set<String> aliases = new HashSet<>();
        aliases.add(requireNonBlank(qc.getAlias(), "alias"));

        validateJoins(qc.getJoins(), aliases);
        validateSelect(qc.getSelect(), aliases);
        validateFilters(qc.getFilters(), aliases);
        validateGroupBy(qc.getGroupBy(), aliases);
        validateHaving(qc.getHaving(), aliases);
        validateOrderBy(qc.getOrderBy(), aliases);
        validateCtes(dto.getCtes());
    }

    // BASE VALIDATION (schema, table, alias)
    private void validateBaseSchemaAndTable(QueryComponentDTO qc) {
        String schema = trimOrNull(qc.getSchema());
        String table = requireNonBlank(qc.getTable(), "table");
        String alias = requireNonBlank(qc.getAlias(), "alias");

        if (schema != null) {
            ve.requireSafeIdentifier(schema);
            ve.requireSafeIdentifier(table);
            ve.requireAllowedTable(schema + "." + table);
        } else {
            ve.requireAllowedTable(table);
        }
        ve.requireSafeIdentifier(alias);
    }

    // JOINS
    private void validateJoins(List<JoinDTO> joins, Set<String> aliases) {
        if (joins == null) return;
        for (JoinDTO j : joins) {
            requireNonNull(j, "join");

            String jAlias = requireNonBlank(j.getAlias(), "join.alias");
            ve.requireSafeIdentifier(jAlias);
            aliases.add(jAlias);

            validateTableName(j.getTable());
            ve.requireAllowedTable(j.getTable());

            String on = trimOrNull(j.getOn());
            if (on != null) ve.validateOnCondition(on, aliases);
        }
    }

    private void validateTableName(String table) {
        table = requireNonBlank(table, "join.table");
        if (table.contains(".")) ve.requireSafeQualified(table);
        else ve.requireSafeIdentifier(table);
    }

    // SELECT
    private void validateSelect(List<SelectItemDTO> select, Set<String> aliases) {
        if (select == null) return;

        for (SelectItemDTO s : select) {
            requireNonNull(s, "select item");

            if (s.getAlias() != null) ve.requireSafeIdentifier(s.getAlias());

            switch (requireNonNull(s.getKind(), "select.kind")) {
                case COLUMN -> validateBareOrQualifiedColumn(
                        requireNonBlank(s.getName(), "select.name"), aliases, "select.name"
                );
                case RAW_EXPRESSION -> validateSafeRawExpression(
                        requireNonBlank(s.getExpression(), "select.expression"), aliases, "select.expression"
                );
                case CASE -> validateCaseExpression(s.getCaseExpr(), aliases);
                case AGGREGATE -> validateAggregate(s.getAggregate(), aliases);
                case WINDOW -> validateWindow(s.getWindow(), aliases);
                default -> throw new IllegalArgumentException("Unsupported select kind");
            }
        }
    }

    // FILTERS (supports nested filter trees)
    private void validateFilters(List<FilterComponentDTO> filters, Set<String> aliases) {
        if (filters == null) return;
        for (FilterComponentDTO f : filters) validateFilterComponent(f, aliases);
    }

    private void validateFilterComponent(FilterComponentDTO component, Set<String> aliases) {
        requireNonNull(component, "filter component");

        if (component.getFilter() != null)
            validateSingleFilter(component.getFilter(), aliases);

        if (component.getGroups() != null)
            for (FilterComponentDTO child : component.getGroups())
                validateFilterComponent(child, aliases);
    }

    private void validateSingleFilter(FilterDTO f, Set<String> aliases) {
        requireNonNull(f, "filter");

        validateBareOrQualifiedColumn(
                requireNonBlank(f.getColumn(), "filter.column"),
                aliases,
                "filter.column"
        );

        Operator op = validateOperator(f.getOperator(), "filter.operator");
        Object val = f.getValue();

        switch (op) {
            case IN, NOT_IN -> {
                if (!(val instanceof Collection<?> col) || col.isEmpty())
                    throw new IllegalArgumentException("IN/NOTIN requires non-empty list");
            }
            case BETWEEN -> {
                if (!isBetweenShape(val))
                    throw new IllegalArgumentException("BETWEEN requires {lo,hi} or size-2 list");
            }
            default -> {
            }
        }
    }

    // GROUP BY
    private void validateGroupBy(List<String> groupBy, Set<String> aliases) {
        if (groupBy == null) return;
        for (String g : groupBy)
            validateBareOrQualifiedColumn(requireNonBlank(g, "groupBy"), aliases, "groupBy");
    }

    // HAVING
    private void validateHaving(List<HavingComponentDTO> having, Set<String> aliases) {
        if (having == null) return;
        for (HavingComponentDTO h : having)
            validateHavingComponent(h, aliases);
    }

    private void validateHavingComponent(HavingComponentDTO component, Set<String> aliases) {
        requireNonNull(component, "having component");

        if (component.getHavingDTO() != null)
            validateSingleHaving(component.getHavingDTO(), aliases);

        if (component.getGroups() != null)
            for (HavingComponentDTO child : component.getGroups())
                validateHavingComponent(child, aliases);
    }

    private void validateSingleHaving(HavingDTO h, Set<String> aliases) {
        requireNonNull(h, "having");

        // function (SUM, COUNT, etc.)
        ve.requireSafeIdentifier(
                requireNonBlank(h.getFunction(), "having.function")
        );

        // column
        validateBareOrQualifiedColumn(
                requireNonBlank(h.getColumn(), "having.column"),
                aliases,
                "having.column"
        );

        // operator
        Operator op = validateOperator(h.getOperator(), "having.operator");
        Object val = h.getValue();

        switch (op) {
            case IN, NOT_IN -> {
                if (!(val instanceof Collection<?> col) || col.isEmpty())
                    throw new IllegalArgumentException("HAVING IN/NOT_IN requires non-empty list");
            }
            case BETWEEN -> {
                if (!isBetweenShape(val))
                    throw new IllegalArgumentException("HAVING BETWEEN requires {lo,hi} or size-2 list");
            }
            default -> {
                // scalar operators: =, >, <, >=, <=, etc.
            }
        }
    }



    // ORDER BY
    private void validateOrderBy(List<OrderByDTO> orders, Set<String> aliases) {
        if (orders == null) return;

        for (OrderByDTO ob : orders) {
            requireNonNull(ob, "orderBy");

            validateBareOrQualifiedColumn(
                    requireNonBlank(ob.getColumn(), "orderBy.column"),
                    aliases,
                    "orderBy.column"
            );

            String dir = trimOrNull(ob.getDirection());
            if (dir != null && !DIRECTION.contains(dir.toUpperCase(Locale.ROOT)))
                throw new IllegalArgumentException("Invalid order direction: " + dir);
        }
    }

    // --------------------------------------------------------------------------------------------
    // CTE VALIDATION
    // --------------------------------------------------------------------------------------------
    private void validateCtes(List<CteDTO> ctes) {
        if (ctes == null) return;

        for (CteDTO cte : ctes) {
            requireNonNull(cte, "cte");
            ve.requireSafeIdentifier(requireNonBlank(cte.getName(), "cte.name"));
            QueryComponentDTO qComp = requireNonNull(cte.getQueryComponentDTO(), "cte.queryComponentDTO");
            // CTE gets validated as a normal QueryComponent, but with a fresh alias set
            Set<String> aliases = new HashSet<>();
            aliases.add(requireNonBlank(qComp.getAlias(), "cte.queryComponentDTO.alias"));

            validateBaseSchemaAndTable(qComp);
            validateJoins(qComp.getJoins(), aliases);
            validateSelect(qComp.getSelect(), aliases);
            validateFilters(qComp.getFilters(), aliases);
            validateGroupBy(qComp.getGroupBy(), aliases);
            validateHaving(qComp.getHaving(), aliases);
            validateOrderBy(qComp.getOrderBy(), aliases);
            // Limit/offset could also be validated if needed
        }
    }

    // --------------------------------------------------------------------------------------------
    // CASE EXPRESSION VALIDATION
    // --------------------------------------------------------------------------------------------
    private void validateCaseExpression(CaseExpressionDTO c, Set<String> aliases) {
        requireNonNull(c, "case expression");

        List<WhenClauseDTO> whens = requireNonEmpty(c.getWhens(), "case.whens");
        for (WhenClauseDTO w : whens) {
            ve.validateOnCondition(requireNonBlank(w.getCondition(), "case.when.condition"), aliases);
            requireSafeLiteral(requireNonBlank(w.getResult(), "case.when.result"), "case.when.result");
        }

        if (c.getElseResult() != null)
            requireSafeLiteral(c.getElseResult(), "case.else");

        if (c.getAlias() != null)
            ve.requireSafeIdentifier(c.getAlias());
    }

    // --------------------------------------------------------------------------------------------
    // AGGREGATE VALIDATION
    // --------------------------------------------------------------------------------------------
    private void validateAggregate(AggregateDTO a, Set<String> aliases) {
        requireNonNull(a, "aggregate");

        ve.requireSafeIdentifier(a.getFn());

        for (String arg : requireNonEmpty(a.getArgs(), "aggregate.args"))
            validateBareOrQualifiedColumn(arg, aliases, "aggregate.arg");

        if (a.getSeparator() != null)
            requireSafeLiteral(a.getSeparator(), "aggregate.separator");

        if (a.getOrderByWithin() != null)
            for (Object ob : a.getOrderByWithin())
                validateOrderByToken(ob.toString(), aliases, "aggregate.orderByWithin");

        if (a.getFilterWhere() != null)
            ve.validateOnCondition(a.getFilterWhere(), aliases);

        if (a.getWithinGroupOrderBy() != null)
            validateOrderByRaw(a.getWithinGroupOrderBy(), aliases, "aggregate.withinGroupOrderBy");

        if (a.getOverPartitionBy() != null)
            for (Object o : a.getOverPartitionBy())
                validateBareOrQualifiedColumn(o.toString(), aliases, "aggregate.partitionBy");

        if (a.getOverOrderBy() != null)
            for (Object o : a.getOverOrderBy())
                validateOrderByToken(o.toString(), aliases, "aggregate.over.orderBy");

        if (a.getFrameClause() != null)
            requireSafeFrameClause(a.getFrameClause());

        if (a.getAlias() != null)
            ve.requireSafeIdentifier(a.getAlias());
    }

    // --------------------------------------------------------------------------------------------
    // WINDOW FUNCTION VALIDATION
    // --------------------------------------------------------------------------------------------
    private void validateWindow(WindowFunctionDTO w, Set<String> aliases) {
        requireNonNull(w, "window");

        validateWindowFunctionName(w.getFunctionName(), aliases);

        if (w.getPartitionBy() != null)
            for (String col : splitCsv(w.getPartitionBy()))
                validateBareOrQualifiedColumn(col, aliases, "window.partitionBy");

        if (w.getOrderBy() != null)
            validateOrderByRaw(w.getOrderBy(), aliases, "window.orderBy");

        if (w.getFrameClause() != null)
            requireSafeFrameClause(w.getFrameClause());

        if (w.getAlias() != null)
            ve.requireSafeIdentifier(w.getAlias());
    }

    // --------------------------------------------------------------------------------------------
    // BASIC VALIDATORS
    // --------------------------------------------------------------------------------------------
    private void validateBareOrQualifiedColumn(String token, Set<String> aliases, String field) {
        token = requireNonBlank(token, field);

        if (!token.contains(".")) {
            ve.requireSafeIdentifier(token);
            ve.requireAllowedColumn(qc.getAlias(), token);
            return;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 2)
            throw new IllegalArgumentException("Invalid column token: " + token);

        String alias = parts[0];
        String col = parts[1];

        ve.requireSafeIdentifier(alias);
        ve.requireSafeIdentifier(col);
        ve.requireAllowedColumn(alias, col);


        if (!aliases.contains(alias))
            throw new IllegalArgumentException("Unknown alias: " + alias);
    }

    private Operator validateOperator(String opRaw, String field) {
        try {
            return Operator.valueOf(requireNonBlank(opRaw, field).toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid operator: " + opRaw);
        }
    }

    private boolean isBetweenShape(Object v) {
        return (v instanceof List<?> list && list.size() == 2) ||
                (v instanceof Map<?, ?> map && map.containsKey("lo") && map.containsKey("hi"));
    }

    private int countPlaceholders(String sql) {
        if (sql == null) return 0;
        int c = 0;
        for (char ch : sql.toCharArray()) if (ch == '?') c++;
        return c;
    }

    private void requireSafeLiteral(String s, String field) {
        s = requireNonBlank(s, field).trim();
        String lower = s.toLowerCase(Locale.ROOT);

        if (NUMERIC_LITERAL.matcher(s).matches()) return;
        if (STRING_LITERAL.matcher(s).matches()) return;
        if (lower.equals("true") || lower.equals("false") || lower.equals("null")) return;

        throw new IllegalArgumentException("Unsafe literal: " + s);
    }

    private void validateOrderByToken(String token, Set<String> aliases, String field) {
        token = requireNonBlank(token, field);
        String[] toks = token.split("\\s+");

        validateBareOrQualifiedColumn(toks[0], aliases, field);

        if (toks.length == 2) {
            String dir = toks[1].toUpperCase(Locale.ROOT);
            if (!DIRECTION.contains(dir))
                throw new IllegalArgumentException("Invalid direction: " + toks[1]);
        }
    }

    private void validateOrderByRaw(String raw, Set<String> aliases, String field) {
        for (String part : splitCsv(raw))
            validateOrderByToken(part, aliases, field);
    }

    private void validateWindowFunctionName(String fn, Set<String> aliases) {
        fn = requireNonBlank(fn, "window.functionName");
        if (fn.contains(";"))
            throw new IllegalArgumentException("Window function must not contain semicolon");

        int open = fn.indexOf('(');
        int close = fn.lastIndexOf(')');

        if (open == -1 || close == -1) {
            ve.requireSafeIdentifier(fn);
            return;
        }

        if (open == 0 || close != fn.length() - 1)
            throw new IllegalArgumentException("Bad window function: " + fn);

        String name = fn.substring(0, open).trim();
        ve.requireSafeIdentifier(name);

        String inner = fn.substring(open + 1, close).trim();
        if (inner.isEmpty()) return;

        if (inner.equals("*") && name.equalsIgnoreCase("COUNT"))
            return;

        for (String arg : splitCsv(inner))
            validateBareOrQualifiedColumn(arg, aliases, "window.functionName.args");
    }

    private void requireSafeFrameClause(String frame) {
        frame = requireNonBlank(frame, "frameClause").trim();

        if (frame.contains(";"))
            throw new IllegalArgumentException("frameClause must not contain semicolon");

        String re = "(?i)^(CURRENT ROW|ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW|"
                + "ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING|"
                + "RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW|"
                + "RANGE BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING)$";

        if (!frame.matches(re))
            throw new IllegalArgumentException("Invalid frameClause: " + frame);
    }

    private static String trimOrNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static <T> T requireNonNull(T obj, String field) {
        if (obj == null)
            throw new IllegalArgumentException(field + " is required");
        return obj;
    }

    private static String requireNonBlank(String s, String field) {
        if (s == null || s.isBlank())
            throw new IllegalArgumentException(field + " is required");
        return s;
    }

    private static <T> List<T> requireNonEmpty(List<T> list, String field) {
        if (list == null || list.isEmpty())
            throw new IllegalArgumentException(field + " is required");
        return list;
    }

    private static List<String> splitCsv(String raw) {
        List<String> out = new ArrayList<>();
        for (String p : raw.split(",")) {
            if (!p.isBlank()) out.add(p.trim());
        }
        return out;
    }

    // --------------------------------------------------------------------------------------------
// RAW EXPRESSION VALIDATION
// --------------------------------------------------------------------------------------------
    private void validateSafeRawExpression(String expr, Set<String> aliases, String field) {
        String e = requireNonBlank(expr, field).trim();
        if (e.contains(";"))
            throw new IllegalArgumentException(field + " must not contain semicolons");
        for (String tok : e.split("\\s+")) {
            String t = tok.replaceAll("[(),]", "").trim();
            if (t.isEmpty()) continue;

            if (STRING_LITERAL.matcher(t).matches()) continue;
            if (NUMERIC_LITERAL.matcher(t).matches()) continue;

            String tl = t.toLowerCase(Locale.ROOT);
            if (Set.of("case", "when", "then", "else", "end", "coalesce", "nullif", "greatest", "least").contains(tl))
                continue;

            if (t.contains(".")) {
                String[] q = t.split("\\.");
                if (q.length != 2)
                    throw new IllegalArgumentException("Bad field token: " + t);
                ve.requireSafeIdentifier(q[0]);
                ve.requireSafeIdentifier(q[1]);
                if (!aliases.contains(q[0]))
                    throw new IllegalArgumentException("Unknown alias: " + q[0]);
            } else {
                ve.requireSafeIdentifier(t);
            }
        }
    }

}

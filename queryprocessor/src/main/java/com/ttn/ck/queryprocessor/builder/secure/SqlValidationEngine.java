package com.ttn.ck.queryprocessor.builder.secure;

import com.ttn.ck.queryprocessor.builder.model.OrderBy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class SqlValidationEngine {
    public static final class Config {
        public final Map<String, Set<String>> tableToAllowedColumns = new HashMap<>();
        public final Set<String> allowedTables = new HashSet<>();
        public final Set<String> allowedSchemas = new HashSet<>();
        public int maxLimit = 100000;
        public int maxOffset = 100000;
        public boolean allowQualified = true; // allow alias.column or schema.table

        public Config allowTable(String table) {
            allowedTables.add(table);
            return this;
        }

        public Config allowSchema(String schema) {
            allowedSchemas.add(schema);
            return this;
        }

        public Config allowColumns(String table, Set<String> cols) {
            tableToAllowedColumns.put(table, cols);
            return this;
        }

    }

    private static final Pattern IDENT = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Pattern QUALIFIED = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");
    private static final Pattern VARIANT_ACCESS =
            Pattern.compile(
                    "[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*\\['[A-Za-z0-9_\\-]+'\\]"
            );

    private final Config cfg;

    public SqlValidationEngine(Config cfg) {
        this.cfg = cfg;
    }

    // Identifier validators
    public String requireSafeIdentifier(String name) {
       return requireSafeColumnExpression(name);
    }

    public String requireSafeColumnExpression(String expr) {
        if (expr == null) {
            throw new IllegalArgumentException("Unsafe column expression: null");
        }

        if (IDENT.matcher(expr).matches()
                || (cfg.allowQualified && QUALIFIED.matcher(expr).matches())
                || VARIANT_ACCESS.matcher(expr).matches()) {
            return expr;
        }

        throw new IllegalArgumentException("Unsafe column expression: " + expr);
    }

    public String requireSafeQualified(String name) {
        if (name == null || !(cfg.allowQualified ? QUALIFIED : IDENT).matcher(name).matches()) {
            throw new IllegalArgumentException("Unsafe qualified identifier: " + name);
        }
        return name;
    }

    public void requireAllowedTable(String table) {
        requireSafeQualified(table);
        if (!cfg.allowedTables.isEmpty() && !cfg.allowedTables.contains(table)) {
            throw new IllegalArgumentException("Table not allowed: " + table);
        }
        if (table.contains(".")) {
            String schema = table.substring(0, table.indexOf('.'));
            if (!cfg.allowedSchemas.isEmpty() && !cfg.allowedSchemas.contains(schema)) {
                throw new IllegalArgumentException("Schema not allowed: " + schema);
            }
        }
    }

    private static final Pattern VARIANT_BASE =
            Pattern.compile(
                    "^(?:[A-Za-z_][A-Za-z0-9_]*\\.)?" + // optional alias.
                            "([A-Za-z_][A-Za-z0-9_]*)" +        // base column (capture)
                            "\\[.*\\]$"                         // anything inside brackets
            );

    private String normalizeVariantColumn(String columnOrExpr) {
        if (columnOrExpr == null) return null;

        var m = VARIANT_BASE.matcher(columnOrExpr);
        if (m.matches()) {
            return m.group(1); // TAG_VARIANT
        }

        return columnOrExpr;
    }

    public void requireAllowedColumn(String tableOrAlias, String columnOrExpr) {

        // Validate identifier / qualified / variant expression
        requireSafeColumnExpression(columnOrExpr);


        String normalizedColumn = normalizeVariantColumn(columnOrExpr);


        requireSafeIdentifier(normalizedColumn);

        Set<String> allowed = cfg.tableToAllowedColumns.get(tableOrAlias);
        if (allowed != null && !allowed.contains(normalizedColumn)) {
            throw new IllegalArgumentException(
                    "Column not allowed: " + columnOrExpr
            );
        }
    }


    // ORDER BY parsing and validation (raw -> vetted OrderBy)
    public OrderBy parseAndValidateOrderBy(String raw, String tableOrAlias) {
        if (raw == null || raw.isBlank()) return new OrderBy();
        OrderBy.OrderByBuilder b = OrderBy.builder();
        for (String part : raw.split(",")) {
            String p = part.trim();
            String[] toks = p.split("\\s+");
            if (toks.length == 0) continue;
            String colToken = toks[0];
            String order = (toks.length >= 2 && toks[1].equalsIgnoreCase("DESC")) ? "DESC" : "ASC";

            String colName;
            String qualifier = tableOrAlias;
            if (colToken.contains(".")) {
                String[] q = colToken.split("\\.");
                if (q.length != 2) throw new IllegalArgumentException("Bad column token: " + colToken);
                qualifier = requireSafeIdentifier(q[0]);
                colName = requireSafeIdentifier(q[1]);
            } else {
                colName = requireSafeIdentifier(colToken);
            }
            requireAllowedColumn(qualifier, colName);
            if ("DESC".equals(order)) b.desc(qualifier != null ? qualifier + "." + colName : colName);
            else b.asc(qualifier != null ? qualifier + "." + colName : colName);
        }
        return b.build();
    }

    // LIMIT/OFFSET sanitizers
    public int sanitizeLimit(Integer input, int defaultLimit) {
        int v = (input == null) ? defaultLimit : input;
        if (v < 0) v = defaultLimit;
        if (v > cfg.maxLimit) v = cfg.maxLimit;
        return v;
    }

    public int sanitizeOffset(Integer input, int defaultOffset) {
        int v = (input == null) ? defaultOffset : input;
        if (v < 0) v = defaultOffset;
        if (v > cfg.maxOffset) v = cfg.maxOffset;
        return v;
    }

    // Simple ON condition validator for equi-joins: alias.col = alias.col [AND ...]
    public String validateOnCondition(String on, Set<String> allowedAliases) {
        if (on == null || on.isBlank()) return on;
        String norm = on.replaceAll("\\s+", " ").trim();
        for (String token : norm.split(" ")) {
            if ("=".equals(token) || "AND".equalsIgnoreCase(token) || "OR".equalsIgnoreCase(token) ||
                    "(".equals(token) || ")".equals(token)) {
                continue;
            }
            // Expect alias.column tokens
            if (token.contains(".")) {
                String[] q = token.split("\\.");
                if (q.length != 2) throw new IllegalArgumentException("Bad ON token: " + token);
                String alias = requireSafeIdentifier(q[0]);
                if (!allowedAliases.contains(alias)) {
                    throw new IllegalArgumentException("Alias not allowed in ON: " + alias);
                }
                // optional: per-alias allowed columns can be enforced externally if configured
            } else {
                throw new IllegalArgumentException("Unexpected token in ON: " + token);
            }
        }
        return norm;
    }
}

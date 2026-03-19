package com.ttn.ck.queryprocessor.builder.model;

import static com.ttn.ck.queryprocessor.builder.utils.ApplicationConstants.*;

/**
 * SQL Operators Enum - Centralizes all SQL operators and functions
 * Enables dynamic filter building for integration engineers
 * 
 * Usage:
 *   Operator.EQUALS.apply("status", "active")
 *   Operator.IN.apply("id", Arrays.asList("1", "2", "3"))
 */
public enum Operator {
    
    // ============================================================
    // COMPARISON OPERATORS
    // ============================================================
    
    EQUALS("=", OperatorType.BINARY, ValueType.SINGLE) {
        @Override
        public String apply(String column, Object value) {
            return column + EQUAL + formatValue(value);
        }
    },
    
    NOT_EQUALS("!=", OperatorType.BINARY, ValueType.SINGLE) {
        @Override
        public String apply(String column, Object value) {
            return column + NOT_EQUAL + formatValue(value);
        }
    },
    
    GREATER_THAN(">", OperatorType.BINARY, ValueType.SINGLE) {
        @Override
        public String apply(String column, Object value) {
            return column + GREATER_THAN + formatValue(value);
        }
    },
    
    GREATER_THAN_OR_EQUAL(">=", OperatorType.BINARY, ValueType.SINGLE) {
        @Override
        public String apply(String column, Object value) {
            return column + GREATER_THAN_EQUAL + formatValue(value);
        }
    },
    
    LESS_THAN("<", OperatorType.BINARY, ValueType.SINGLE) {
        @Override
        public String apply(String column, Object value) {
            return column + LESS_THAN + formatValue(value);
        }
    },
    
    LESS_THAN_OR_EQUAL("<=", OperatorType.BINARY, ValueType.SINGLE) {
        @Override
        public String apply(String column, Object value) {
            return column + LESS_THAN_EQUAL + formatValue(value);
        }
    },
    
    // ============================================================
    // PATTERN MATCHING
    // ============================================================
    
    LIKE("LIKE", OperatorType.BINARY, ValueType.SINGLE) {
        @Override
        public String apply(String column, Object value) {
            return column + LIKE + SINGLE_QUOTE + value + SINGLE_QUOTE;
        }
    },
    
    NOT_LIKE("NOT LIKE", OperatorType.BINARY, ValueType.SINGLE) {
        @Override
        public String apply(String column, Object value) {
            return column + " NOT LIKE " + SINGLE_QUOTE + value + SINGLE_QUOTE;
        }
    },
    
    // ============================================================
    // LIST OPERATORS
    // ============================================================
    
    IN("IN", OperatorType.BINARY, ValueType.LIST) {
        @Override
        public String apply(String column, Object value) {
            return column + IN + formatList(value);
        }
    },
    
    NOT_IN("NOT IN", OperatorType.BINARY, ValueType.LIST) {
        @Override
        public String apply(String column, Object value) {
            return column + NOT_IN + formatList(value);
        }
    },
    
    // ============================================================
    // RANGE OPERATORS
    // ============================================================
    
    BETWEEN("BETWEEN", OperatorType.TERNARY, ValueType.RANGE) {
        @Override
        public String apply(String column, Object value) {
            if (value instanceof Object[] range && range.length == 2) {
                return column + BETWEEN + formatValue(range[0]) + AND + formatValue(range[1]);
            }
            throw new IllegalArgumentException("BETWEEN requires array of 2 values");
        }
    },
    
    NOT_BETWEEN("NOT BETWEEN", OperatorType.TERNARY, ValueType.RANGE) {
        @Override
        public String apply(String column, Object value) {
            if (value instanceof Object[] range && range.length == 2) {
                return column + " NOT BETWEEN " + formatValue(range[0]) + AND + formatValue(range[1]);
            }
            throw new IllegalArgumentException("NOT BETWEEN requires array of 2 values");
        }
    },
    
    // ============================================================
    // NULL OPERATORS
    // ============================================================
    
    IS_NULL("IS NULL", OperatorType.UNARY, ValueType.NONE) {
        @Override
        public String apply(String column, Object value) {
            return column + IS_NULL;
        }
    },
    
    IS_NOT_NULL("IS NOT NULL", OperatorType.UNARY, ValueType.NONE) {
        @Override
        public String apply(String column, Object value) {
            return column + IS_NOT_NULL;
        }
    },
    
    // ============================================================
    // STRING FUNCTIONS (used as operators in filters)
    // ============================================================
    
    STARTS_WITH("STARTS_WITH", OperatorType.FUNCTION, ValueType.SINGLE) {
        @Override
        public String apply(String column, Object value) {
            return column + LIKE + SINGLE_QUOTE + value + "%" + SINGLE_QUOTE;
        }
    },
    
    ENDS_WITH("ENDS_WITH", OperatorType.FUNCTION, ValueType.SINGLE) {
        @Override
        public String apply(String column, Object value) {
            return column + LIKE + SINGLE_QUOTE + "%" + value + SINGLE_QUOTE;
        }
    },
    
    CONTAINS("CONTAINS", OperatorType.FUNCTION, ValueType.SINGLE) {
        @Override
        public String apply(String column, Object value) {
            return column + LIKE + SINGLE_QUOTE + "%" + value + "%" + SINGLE_QUOTE;
        }
    },
    
    NOT_CONTAINS("NOT_CONTAINS", OperatorType.FUNCTION, ValueType.SINGLE) {
        @Override
        public String apply(String column, Object value) {
            return column + " NOT LIKE " + SINGLE_QUOTE + "%" + value + "%" + SINGLE_QUOTE;
        }
    };
    
    // ============================================================
    // ENUM PROPERTIES
    // ============================================================
    
    private final String symbol;
    private final OperatorType operatorType;
    private final ValueType valueType;
    
    Operator(String symbol, OperatorType operatorType, ValueType valueType) {
        this.symbol = symbol;
        this.operatorType = operatorType;
        this.valueType = valueType;
    }
    
    // ============================================================
    // ABSTRACT METHOD
    // ============================================================
    
    /**
     * Apply the operator to column and value(s)
     * @param column The column name
     * @param value The value(s) - can be single, list, or array depending on operator
     * @return SQL condition string
     */
    public abstract String apply(String column, Object value);
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Format single value based on type
     */
    protected String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof String) {
            return SINGLE_QUOTE + value + SINGLE_QUOTE;
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        // Default: treat as string
        return SINGLE_QUOTE + value.toString() + SINGLE_QUOTE;
    }
    
    /**
     * Format list of values
     */
    protected String formatList(Object value) {
        if (value instanceof Iterable) {
            StringBuilder sb = new StringBuilder(OPEN_BRACKET);
            boolean first = true;
            for (Object item : (Iterable<?>) value) {
                if (!first) sb.append(COMMA_DELIMETER);
                sb.append(formatValue(item));
                first = false;
            }
            sb.append(CLOSE_BRACKET);
            return sb.toString();
        }
        throw new IllegalArgumentException("Value must be Iterable for list operators");
    }
    
    // ============================================================
    // GETTERS
    // ============================================================
    
    public String getSymbol() {
        return symbol;
    }
    
    public OperatorType getOperatorType() {
        return operatorType;
    }
    
    public ValueType getValueType() {
        return valueType;
    }
    
    // ============================================================
    // METADATA ENUMS
    // ============================================================
    
    /**
     * Type of operator based on number of operands
     */
    public enum OperatorType {
        UNARY,      // column only (IS NULL)
        BINARY,     // column + value (=, >, <, LIKE)
        TERNARY,    // column + 2 values (BETWEEN)
        FUNCTION    // function-like (STARTS_WITH, CONTAINS)
    }
    
    /**
     * Type of value expected
     */
    public enum ValueType {
        NONE,       // No value needed
        SINGLE,     // Single value
        LIST,       // List of values
        RANGE       // Range (2 values)
    }
}
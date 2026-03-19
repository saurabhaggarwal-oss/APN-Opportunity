package com.ttn.ck.queryprocessor.builder.utils;

/**
 * Application constants for SQL keywords and delimiters
 * Add any missing constants here
 */
public class ApplicationConstants {
    
    // SQL Keywords
    public static final String SELECT = " SELECT ";
    public static final String FROM = " FROM ";
    public static final String WHERE = " WHERE ";
    public static final String INSERT_INTO = " INSERT INTO ";
    public static final String VALUES = " VALUES ";
    public static final String UPDATE = " UPDATE ";
    public static final String SET = " SET ";
    public static final String DELETE = " DELETE ";
    public static final String GROUP_BY = " GROUP BY ";
    public static final String ORDER_BY = " ORDER BY ";
    public static final String AS = " AS ";
    
    // Operators
    public static final String AND = " AND ";
    public static final String OR = " OR ";
    public static final String EQUAL = " = ";
    public static final String NOT_EQUAL = " != ";
    public static final String GREATER_THAN = " > ";
    public static final String GREATER_THAN_EQUAL = " >= ";
    public static final String LESS_THAN = " < ";
    public static final String LESS_THAN_EQUAL = " <= ";
    public static final String LIKE = " LIKE ";
    public static final String IN = " IN ";
    public static final String NOT_IN = " NOT IN ";
    public static final String BETWEEN = " BETWEEN ";
    public static final String IS_NULL = " IS NULL ";
    public static final String IS_NOT_NULL = " IS NOT NULL ";
    
    // Aggregate Functions
    public static final String SUM = "SUM";
    public static final String COUNT = "COUNT";
    public static final String AVG = "AVG";
    public static final String MIN = "MIN";
    public static final String MAX = "MAX";
    public static final String ROUND = "ROUND";
    
    // Delimiters and Symbols
    public static final String COMMA_DELIMETER = ", ";
    public static final String SPACE = " ";
    public static final String OPEN_BRACKET = "(";
    public static final String CLOSE_BRACKET = ")";
    public static final String SINGLE_QUOTE = "'";
    public static final String DOUBLE_QUOTE = "\"";
    
    private ApplicationConstants() {
        // Private constructor to prevent instantiation
    }
}

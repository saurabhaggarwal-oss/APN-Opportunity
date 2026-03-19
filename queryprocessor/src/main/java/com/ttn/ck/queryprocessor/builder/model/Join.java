package com.ttn.ck.queryprocessor.builder.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static com.ttn.ck.queryprocessor.utils.ApplicationConstants.SPACE;

/**
 * Represents JOIN clauses in SQL query with support for different join types
 */
@Data
@AllArgsConstructor
public class Join {
    private List<JoinClause> joinClauses;
    
    public Join() {
        this.joinClauses = new ArrayList<>();
    }
    
    public static JoinBuilder builder() {
        return new JoinBuilder();
    }
    
    public String toSql() {
        if (joinClauses == null || joinClauses.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        for (JoinClause clause : joinClauses) {
            sql.append(clause.toSql());
        }
        return sql.toString();
    }
    
    public boolean isEmpty() {
        return joinClauses == null || joinClauses.isEmpty();
    }
    
    /**
     * Enum for join types
     */
    public enum JoinType {
        INNER("INNER JOIN"),
        LEFT("LEFT JOIN"),
        RIGHT("RIGHT JOIN"),
        FULL("FULL OUTER JOIN"),
        CROSS("CROSS JOIN");
        
        private final String sql;
        
        JoinType(String sql) {
            this.sql = sql;
        }
        
        public String getSql() {
            return sql;
        }
    }
    
    /**
     * Inner class representing a single JOIN clause
     */
    @Data
    @AllArgsConstructor
    public static class JoinClause {
        private JoinType joinType;
        private String tableName;
        private String alias;
        private String onCondition;
        
        public JoinClause(JoinType joinType, String tableName, String onCondition) {
            this.joinType = joinType;
            this.tableName = tableName;
            this.alias = null;
            this.onCondition = onCondition;
        }
        
        public String toSql() {
            StringBuilder sql = new StringBuilder();
            sql.append(SPACE).append(joinType.getSql()).append(SPACE).append(tableName);
            
            if (alias != null && !alias.isEmpty()) {
                sql.append(SPACE).append(alias);
            }
            
            if (onCondition != null && !onCondition.isEmpty()) {
                sql.append(SPACE).append("ON").append(SPACE).append(onCondition);
            }
            
            return sql.toString();
        }
    }
    
    public static class JoinBuilder {
        private final List<JoinClause> joinClauses = new ArrayList<>();
        
        /**
         * Add an INNER JOIN
         */
        public JoinBuilder innerJoin(String tableName, String onCondition) {
            this.joinClauses.add(new JoinClause(JoinType.INNER, tableName, onCondition));
            return this;
        }
        
        /**
         * Add an INNER JOIN with table alias
         */
        public JoinBuilder innerJoin(String tableName, String alias, String onCondition) {
            this.joinClauses.add(new JoinClause(JoinType.INNER, tableName, alias, onCondition));
            return this;
        }
        
        /**
         * Add a LEFT JOIN
         */
        public JoinBuilder leftJoin(String tableName, String onCondition) {
            this.joinClauses.add(new JoinClause(JoinType.LEFT, tableName, onCondition));
            return this;
        }
        
        /**
         * Add a LEFT JOIN with table alias
         */
        public JoinBuilder leftJoin(String tableName, String alias, String onCondition) {
            this.joinClauses.add(new JoinClause(JoinType.LEFT, tableName, alias, onCondition));
            return this;
        }
        
        /**
         * Add a RIGHT JOIN
         */
        public JoinBuilder rightJoin(String tableName, String onCondition) {
            this.joinClauses.add(new JoinClause(JoinType.RIGHT, tableName, onCondition));
            return this;
        }
        
        /**
         * Add a RIGHT JOIN with table alias
         */
        public JoinBuilder rightJoin(String tableName, String alias, String onCondition) {
            this.joinClauses.add(new JoinClause(JoinType.RIGHT, tableName, alias, onCondition));
            return this;
        }
        
        /**
         * Add a FULL OUTER JOIN
         */
        public JoinBuilder fullJoin(String tableName, String onCondition) {
            this.joinClauses.add(new JoinClause(JoinType.FULL, tableName, onCondition));
            return this;
        }
        
        /**
         * Add a FULL OUTER JOIN with table alias
         */
        public JoinBuilder fullJoin(String tableName, String alias, String onCondition) {
            this.joinClauses.add(new JoinClause(JoinType.FULL, tableName, alias, onCondition));
            return this;
        }
        
        /**
         * Add a CROSS JOIN
         */
        public JoinBuilder crossJoin(String tableName) {
            this.joinClauses.add(new JoinClause(JoinType.CROSS, tableName, null));
            return this;
        }
        
        /**
         * Add a CROSS JOIN with table alias
         */
        public JoinBuilder crossJoin(String tableName, String alias) {
            this.joinClauses.add(new JoinClause(JoinType.CROSS, tableName, alias, null));
            return this;
        }
        
        /**
         * Add a custom join clause
         */
        public JoinBuilder customJoin(JoinType joinType, String tableName, String alias, String onCondition) {
            this.joinClauses.add(new JoinClause(joinType, tableName, alias, onCondition));
            return this;
        }
        
        public Join build() {
            return new Join(joinClauses);
        }
    }
}

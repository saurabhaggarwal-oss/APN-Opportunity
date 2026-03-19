package com.ttn.ck.queryprocessor.builder.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectItemDTO {

    public enum Kind {
        COLUMN,           // simple column name
        RAW_EXPRESSION,   // vendor-neutral raw expression
        CASE,             // CASE expression via CaseExpressionDTO
        AGGREGATE,        // AggregateDTO
        WINDOW            // WindowFunctionDTO
    }

    private Kind kind;

    // Common optional alias for any kind
    private String alias;

    // COLUMN
    private String name;

    // RAW_EXPRESSION
    private String expression;

    // CASE
    private CaseExpressionDTO caseExpr;

    // AGGREGATE
    private AggregateDTO aggregate;

    // WINDOW
    private WindowFunctionDTO window;
}

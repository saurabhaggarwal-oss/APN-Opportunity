package com.ttn.ck.queryprocessor.builder.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregateDTO {
    // Function name: SUM, AVG, MIN, MAX, COUNT, STRING_AGG, ARRAY_AGG, JSON_AGG,
    // STDDEV, VARIANCE, PERCENTILE_DISC, PERCENTILE_CONT, etc.
    private String fn;

    // Arguments: columns or expressions; use "*" via a single arg "*" for COUNT(*)
    private List<String> args;

    // DISTINCT toggle (COUNT DISTINCT, SUM DISTINCT, etc.)
    private boolean distinct;

    // Optional alias applied to the final select item
    private String alias;

    // STRING_AGG separator (optional)
    private String separator;

    // ORDER BY inside aggregate, e.g., STRING_AGG(expr ORDER BY col)
    private List<String> orderByWithin;

    // FILTER (WHERE ...)
    private String filterWhere;

    // Ordered-set WITHIN GROUP (ORDER BY ...)
    private String withinGroupOrderBy;

    // Optional OVER clause for analytic aggregate
    private List<String> overPartitionBy;
    private List<String> overOrderBy;
    private String frameClause;
}

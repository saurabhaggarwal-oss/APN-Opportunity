package com.ttn.ck.queryprocessor.builder.dto;

import lombok.Data;

@Data
public class WindowFunctionDTO {
    private String functionName;  // e.g. ROW_NUMBER(), RANK(), SUM(amount)
    private String partitionBy;   // e.g. "customer_id, region"
    private String orderBy;       // e.g. "order_date DESC, id"
    private String frameClause;   // e.g. "ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW"
    private String alias;         // optional
}

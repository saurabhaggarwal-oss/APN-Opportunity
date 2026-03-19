package com.ttn.ck.queryprocessor.builder.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryComponentDTO {
    private String schema;               // optional
    private String table;                // required
    private String alias;                // required// optional
    private List<SelectItemDTO> select;  // required
    private List<JoinDTO> joins;         // optional
    private List<FilterComponentDTO> filters;     // optional
    private List<String> groupBy;        // optional
    private List<HavingComponentDTO> having;      // optional
    private List<OrderByDTO> orderBy;    // optional
    private Integer limit;               // optional
    private Integer offset;
}

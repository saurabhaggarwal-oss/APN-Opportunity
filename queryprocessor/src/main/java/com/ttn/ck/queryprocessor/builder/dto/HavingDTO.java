package com.ttn.ck.queryprocessor.builder.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HavingDTO {
    private String function; // SUM, COUNT, AVG, ...
    private String column;   // qualified column
    private String operator; // EQUALS, GREATER_THAN, ...
    private Object value;    // bound as ?
    private boolean not;     // optional NOT variants
}

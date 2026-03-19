package com.ttn.ck.queryprocessor.builder.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterDTO {
    private String column;
    private String operator; // EQUALS, IN, BETWEEN, etc. (mapped to Operator)
    private Object value;
}

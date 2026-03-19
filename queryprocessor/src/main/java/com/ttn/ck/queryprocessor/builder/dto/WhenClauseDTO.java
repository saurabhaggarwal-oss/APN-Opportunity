package com.ttn.ck.queryprocessor.builder.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhenClauseDTO {
    private String condition; // validated tokens
    private String result;    // validated literal/expression
}

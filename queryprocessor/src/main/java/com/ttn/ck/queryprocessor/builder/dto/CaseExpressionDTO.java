package com.ttn.ck.queryprocessor.builder.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseExpressionDTO {
    private List<WhenClauseDTO> whens;
    private String elseResult;  // optional
    private String alias;       // optional
}

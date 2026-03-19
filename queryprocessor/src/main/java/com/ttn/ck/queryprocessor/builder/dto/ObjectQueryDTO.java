package com.ttn.ck.queryprocessor.builder.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectQueryDTO {
    private List<CteDTO> ctes;
    private QueryComponentDTO queryComponents;
}

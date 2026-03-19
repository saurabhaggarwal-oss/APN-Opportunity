package com.ttn.ck.queryprocessor.builder.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CteDTO {
    private String name;
    private QueryComponentDTO queryComponentDTO;
}

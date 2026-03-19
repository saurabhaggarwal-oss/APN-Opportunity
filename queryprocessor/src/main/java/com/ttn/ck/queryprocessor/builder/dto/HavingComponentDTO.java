package com.ttn.ck.queryprocessor.builder.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HavingComponentDTO {
    private HavingDTO havingDTO;
    private List<HavingComponentDTO> groups;
    private Relation relation;
}

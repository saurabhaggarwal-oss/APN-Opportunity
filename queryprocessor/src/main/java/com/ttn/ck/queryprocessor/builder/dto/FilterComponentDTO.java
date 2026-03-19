package com.ttn.ck.queryprocessor.builder.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterComponentDTO {
    private FilterDTO filter;
    private List<FilterComponentDTO> groups;
    private Relation relation;
}
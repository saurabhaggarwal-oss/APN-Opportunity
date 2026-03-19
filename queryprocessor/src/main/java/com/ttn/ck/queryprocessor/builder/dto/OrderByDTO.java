package com.ttn.ck.queryprocessor.builder.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderByDTO {
    private String column;    // qualified column
    private String direction; // ASC or DESC
}

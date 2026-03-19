package com.ttn.ck.queryprocessor.builder.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinDTO {
    private String type;   // INNER, LEFT, RIGHT, FULL, CROSS
    private String table;  // schema-qualified allowed
    private String alias;  // required
    private String on;     // validated ON condition
}

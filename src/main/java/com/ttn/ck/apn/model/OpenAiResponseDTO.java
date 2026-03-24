package com.ttn.ck.apn.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiResponseDTO {
    @JsonAlias("$schema")
    private String schema;
    private Object data;
}

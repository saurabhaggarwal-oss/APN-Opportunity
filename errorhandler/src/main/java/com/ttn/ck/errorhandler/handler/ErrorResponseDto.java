package com.ttn.ck.errorhandler.handler;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    private Long timestamp;
    private String error;
    private String message;
    private Integer status;

    public ErrorResponseDto(String message, Integer status) {
        this.status = status;
        this.timestamp = new Date().getTime();
        this.message = message;
    }


}

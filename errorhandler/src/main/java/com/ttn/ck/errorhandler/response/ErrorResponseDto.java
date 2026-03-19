package com.ttn.ck.errorhandler.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * The type Error response dto.
 *
 * @param <T> the type parameter
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ErrorResponseDto<T> implements ResponseDto<T> {

    private Long timestamp;
    private String error;
    private String message;
    private Integer status;
    private T data;


    public ErrorResponseDto(String message, Integer status) {
        this.status = status;
        this.timestamp = new Date().getTime();
        this.message = message;
    }

    public ErrorResponseDto(T data, String message, Integer status) {
        this.status = status;
        this.timestamp = new Date().getTime();
        this.message = message;
        this.data = data;
    }


}

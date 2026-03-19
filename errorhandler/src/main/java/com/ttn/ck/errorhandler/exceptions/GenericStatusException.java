package com.ttn.ck.errorhandler.exceptions;

import lombok.Getter;

@Getter
public class GenericStatusException extends BaseException{

    private final int status;

    public GenericStatusException(String messageKey, int status) {
        super(messageKey);
        this.status = status;
    }
}

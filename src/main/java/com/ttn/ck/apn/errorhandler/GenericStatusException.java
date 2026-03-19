package com.ttn.ck.apn.errorhandler;

import lombok.Getter;

@Getter
public class GenericStatusException extends BaseException{

    private final int status;

    public GenericStatusException(String messageKey, int status) {
        super(messageKey);
        this.status = status;
    }
}
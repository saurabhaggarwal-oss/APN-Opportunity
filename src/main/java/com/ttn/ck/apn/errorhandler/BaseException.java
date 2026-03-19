package com.ttn.ck.apn.errorhandler;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {

    private final String messageKey;

    protected BaseException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

}

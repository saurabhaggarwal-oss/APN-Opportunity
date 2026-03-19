package com.ttn.ck.errorhandler.exceptions;

import lombok.Getter;

@Getter
public class GenericArgsException  extends BaseException {

    private final String[] args;

    public GenericArgsException(String messageKey, String[] args) {
        super(messageKey);
        this.args = args;
    }

}

package com.ttn.ck.errorhandler.exceptions;

import lombok.Getter;

@Getter
public final class GenericException extends BaseException {

    public GenericException(String messageKey) {
        super(messageKey);
    }

}

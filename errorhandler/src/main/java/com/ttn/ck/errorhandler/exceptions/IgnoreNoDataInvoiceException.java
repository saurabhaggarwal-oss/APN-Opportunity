package com.ttn.ck.errorhandler.exceptions;

import lombok.Getter;

@Getter
public final class IgnoreNoDataInvoiceException extends BaseException{

    public IgnoreNoDataInvoiceException(String messageKey) {
        super(messageKey);
    }

}

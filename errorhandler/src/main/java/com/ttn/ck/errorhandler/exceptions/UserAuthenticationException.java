package com.ttn.ck.errorhandler.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class UserAuthenticationException extends BaseException {

    public UserAuthenticationException(String message) {
        super(message);
    }

}

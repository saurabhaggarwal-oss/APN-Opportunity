package com.ttn.ck.errorhandler.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class UserNotFoundException extends BaseException {

    public UserNotFoundException(String messageKey) {
        super(messageKey);
    }

}

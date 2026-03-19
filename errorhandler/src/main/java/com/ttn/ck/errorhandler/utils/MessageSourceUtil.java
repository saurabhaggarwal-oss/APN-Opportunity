package com.ttn.ck.errorhandler.utils;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@AllArgsConstructor
public class MessageSourceUtil {

    private final MessageSource messageSource;

    public String getMessage(String messageKey) {
        return getMessage(messageKey, "");
    }

    public String getMessage(String messageKey, Object... args) {
        try {
            String message = messageSource.getMessage(messageKey, args, LocaleContextHolder.getLocale());
            return StringUtils.hasText(message) ? message : messageKey;
        } catch (Exception ignored) {
            return messageKey;
        }
    }

}

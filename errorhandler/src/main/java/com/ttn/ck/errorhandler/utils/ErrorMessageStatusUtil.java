package com.ttn.ck.errorhandler.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@AllArgsConstructor
public class ErrorMessageStatusUtil {

    private MessageSourceUtil messageSourceUtil;

    public String getMessage(String messageKey) {
        return messageSourceUtil.getMessage(messageKey, "");
    }

    public Integer getStatus(String messageKey) {
        try {
            String status = messageSourceUtil.getMessage(messageKey + ".status");
            return StringUtils.hasText(status) ? parseStatus(status) : 500;
        } catch (Exception ignored) {
            return 500;
        }
    }

    private Integer parseStatus(String status) {
        return Integer.valueOf(status);
    }

    public ErrorStatus getErrorStatus(String message) {
        return new ErrorStatus(messageSourceUtil.getMessage(message), getStatus(message));
    }

    public ErrorStatus getErrorStatus(String message, Object[] args) {
        return new ErrorStatus(messageSourceUtil.getMessage(message, args), getStatus(message));
    }

}

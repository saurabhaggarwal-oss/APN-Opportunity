package com.ttn.ck.errorhandler.exceptions;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.ttn.ck.errorhandler.response.ErrorResponseDto;
import com.ttn.ck.errorhandler.response.ResponseDto;
import com.ttn.ck.errorhandler.utils.ErrorMessageStatusUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static java.util.Objects.nonNull;

/**
 * The type Global exceptions' handler.
 */
@Slf4j
@ControllerAdvice
@RestController
@NoArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionsHandler {

    private static final Set<String> END_POINTS;

    static {
        END_POINTS = Set.of(
                "/user-meta-data/details",
                "/dashboard/all/v4",
                "/customer/mavList",
                "/partner/get",
                "/module/enabled/all",
                "/customer/min-date",
                "/customer/user/all",
                "/module/all",
                "/mav/name/all",
                "/auto-meta/customer",
                "/aws/account-onboard",
                "/azure/customer-onboard",
                "/mav/customer/exist",
                "/user-meta-data/v2",
                "/currency/get",
                "/dates/min-and-max",
                "/customer/auto/metadata"
        );
    }

    @Autowired
    private ErrorMessageStatusUtil errorMessageStatusUtil;

    /*
        To handle jackson bind errors for format in request params
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = InvalidFormatException.class)
    public ResponseDto<String> genericInvalidFormatException(InvalidFormatException e) {
        String message = "Invalid format for request parameter";
        if (!CollectionUtils.isEmpty(e.getPath()) && nonNull(e.getPath().get(0)) && nonNull(e.getValue())) {
            String fieldName = e.getPath().get(0).getFieldName();
            String value = e.getValue().toString();
            message = String.format("Invalid value '%s' for request param '%s'", value, fieldName);
        }
        return new ErrorResponseDto<>(message, 400);
    }

    @ExceptionHandler(value = GenericException.class)
    public ResponseDto<String> genericForbiddenException(GenericException e, HttpServletRequest request, HttpServletResponse response) {
        Integer statusCode = errorMessageStatusUtil.getStatus(e.getMessageKey());
        if (END_POINTS.contains(request.getServletPath()) && statusCode.equals(403)) {
            response.setStatus(709);
            return new ErrorResponseDto<>("Request Failed. Please contact your administrator for assistance.", 709);
        } else {
            response.setStatus(statusCode);
            String message = errorMessageStatusUtil.getMessage(e.getMessage());
            return new ErrorResponseDto<>(message.isEmpty() ? e.getMessageKey() : message, statusCode);
        }
    }

}

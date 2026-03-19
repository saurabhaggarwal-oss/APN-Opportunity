package com.ttn.ck.errorhandler.handler;

import com.ttn.ck.errorhandler.exceptions.*;
import com.ttn.ck.errorhandler.response.ResponseDto;
import com.ttn.ck.errorhandler.utils.ErrorMessageStatusUtil;
import com.ttn.ck.errorhandler.utils.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.concurrent.ExecutionException;

/**
 * The type Global exception handler.
 */
@Slf4j
@Order
@ControllerAdvice
@AllArgsConstructor
@RestController("core-exception-handler")
public class CoreExceptionHandler {

    private final ErrorMessageStatusUtil errorMessageStatusUtil;
    private final HttpServletResponse response;
    private final HttpServletRequest request;


    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ErrorResponseDto methodArgsNotValidException(MethodArgumentNotValidException e) {
        return new ErrorResponseDto(e.getBindingResult().getAllErrors().get(0).getDefaultMessage(), 400);
    }


    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = UserAuthenticationException.class)
    public ErrorResponseDto userAuthenticationException(UserAuthenticationException e) {
        return new ErrorResponseDto(errorMessageStatusUtil.getMessage(e.getMessageKey()), 401);
    }


    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = UserNotFoundException.class)
    public ErrorResponseDto userNotFoundException(UserNotFoundException e) {
        return new ErrorResponseDto(errorMessageStatusUtil.getMessage(e.getMessageKey()), 400);
    }


    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ErrorResponseDto httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return new ErrorResponseDto(e.getMessage(), 405);
    }


    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ErrorResponseDto missingServletRequestParameterException(MissingServletRequestParameterException e) {
        return new ErrorResponseDto("Required request parameter '" + e.getParameterName() + "'", 400);
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {ExecutionException.class,InterruptedException.class})
    public ErrorResponseDto threadInterruptException(Exception e) {
        return new ErrorResponseDto("Unable to process request with error: '" + e.getMessage() + "'", 500);
    }


    @ExceptionHandler(value = GenericException.class)
    public ErrorResponseDto genericException(GenericException e) {
        ErrorStatus errorStatus = errorMessageStatusUtil.getErrorStatus(e.getMessageKey());
        response.setStatus(errorStatus.getStatus());
        return new ErrorResponseDto(errorStatus.getMessage(), errorStatus.getStatus());
    }

    @ExceptionHandler(value = GenericArgsException.class)
    public ErrorResponseDto genericException(GenericArgsException e) {
        ErrorStatus errorStatus = errorMessageStatusUtil.getErrorStatus(e.getMessageKey(), e.getArgs());
        response.setStatus(errorStatus.getStatus());
        return new ErrorResponseDto(errorStatus.getMessage(), errorStatus.getStatus());
    }


    @ExceptionHandler(value = BaseException.class)
    public ErrorResponseDto baseException(BaseException e) {
        ErrorStatus errorStatus = errorMessageStatusUtil.getErrorStatus(e.getMessageKey());
        response.setStatus(errorStatus.getStatus());
        return new ErrorResponseDto(errorStatus.getMessage(), errorStatus.getStatus());
    }


    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = HttpClientErrorException.class)
    public ErrorResponseDto httpClientErrorException(HttpClientErrorException e) {
        return new ErrorResponseDto(e.getMessage(), 403);
    }


    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ErrorResponseDto constraintViolationException(ConstraintViolationException e) {
        return new ErrorResponseDto(e.getConstraintViolations().iterator().next().getMessage(), 400);
    }


    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = Exception.class)
    public ErrorResponseDto exception(Exception e) {
        log.error("Unhandled exception occurred: ", e);
        return new ErrorResponseDto("Something went wrong", 500);
    }


    @ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public ErrorResponseDto exception(HttpMediaTypeNotSupportedException e) {
        return new ErrorResponseDto("Media Type not supported", 415);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseDto<String> genericForbiddenException(HttpMessageNotReadableException e, HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return new com.ttn.ck.errorhandler.response.ErrorResponseDto<>("Invalid or missing request body.", HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ErrorResponseDto illegalArgsException(IllegalArgumentException e) {
        response.setStatus(400);
        return new ErrorResponseDto(e.getMessage(), 400);
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ErrorResponseDto methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        response.setStatus(400);
        return new ErrorResponseDto(e.getMessage(), 400);
    }

    @ExceptionHandler(value = GenericStatusException.class)
    public ErrorResponseDto genericException(GenericStatusException e) {
        response.setStatus(e.getStatus());
        return new ErrorResponseDto(e.getMessage(), e.getStatus());
    }

    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    public ErrorResponseDto maxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        if(request.getMethod().equals(RequestMethod.OPTIONS.name())) {
            response.setStatus(200);
            return null;
        }
        response.setStatus(400);
        return new ErrorResponseDto("Please upload maximum permitted size 1MB file", 400);
    }


    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request) {

        log.warn("Rate limit exceeded: {}", ex.getMessage());

        ErrorResponseDto errorResponseDto= new ErrorResponseDto(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS.value());

        return new ResponseEntity<>(errorResponseDto,HttpStatus.TOO_MANY_REQUESTS);


    }

}

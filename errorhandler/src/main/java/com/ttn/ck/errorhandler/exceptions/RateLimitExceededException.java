package com.ttn.ck.errorhandler.exceptions;


import lombok.Getter;

/**
 * Exception thrown when rate limit is exceeded
 */
@Getter
public class RateLimitExceededException extends RuntimeException {

    private String userId;
    private int limit;
    private int window;

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    public RateLimitExceededException(String userId, int limit, int window) {
        super(String.format("Rate limit exceeded for user %s: %d requests per %d seconds",
                userId, limit, window));
        this.userId = userId;
        this.limit = limit;
        this.window = window;
    }

}

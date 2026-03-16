package com.ttn.ck.apn.exception;

/**
 * Thrown when a requested resource (e.g., opportunity by UUID) is not found
 * in the database.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

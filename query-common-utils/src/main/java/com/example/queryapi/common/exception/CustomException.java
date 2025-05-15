package com.example.queryapi.common.exception;

/**
 * Base exception class for all custom exceptions in the application.
 */
public class CustomException extends RuntimeException {

    public CustomException(String message) {
        super(message);
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }
}

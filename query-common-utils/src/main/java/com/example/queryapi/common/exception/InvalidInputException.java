package com.example.queryapi.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an invalid input is provided to an API or service.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidInputException extends CustomException {

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}


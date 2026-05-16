package com.htekin.loganalyzer.exception;

/**
 * Thrown when the uploaded log file fails validation
 * (empty, wrong extension, exceeds size limit, or is unreadable).
 *
 * <p>Caught by {@link GlobalExceptionHandler} and returned as HTTP 400.
 */
public class InvalidLogFileException extends RuntimeException {

    public InvalidLogFileException(String message) {
        super(message);
    }

    public InvalidLogFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
package com.htekin.loganalyzer.exception;

import io.swagger.v3.oas.annotations.Hidden; // 1. Bu importu ekledik
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.URI;
import java.time.Instant;

@Slf4j
@RestControllerAdvice
@Hidden // 2. Swagger'a "Burayı görmezden gel" dedik
public class GlobalExceptionHandler {

    private static final String ERROR_BASE_URI = "https://securitylog.com/errors/";

    @ExceptionHandler(InvalidLogFileException.class)
    public ProblemDetail handleInvalidLogFile(InvalidLogFileException ex) {
        log.warn("Invalid log file upload rejected: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Log File");
        problem.setType(URI.create(ERROR_BASE_URI + "invalid-file"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("Upload rejected — file too large: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "The uploaded file exceeds the maximum allowed size of 50 MB.");
        problem.setTitle("File Too Large");
        problem.setType(URI.create(ERROR_BASE_URI + "file-too-large"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled exception in request processing", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again or contact support.");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create(ERROR_BASE_URI + "internal-error"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
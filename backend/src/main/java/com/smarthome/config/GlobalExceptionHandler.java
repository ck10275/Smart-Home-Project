package com.smarthome.config;

import com.smarthome.exception.DeviceNotFoundException;
import com.smarthome.exception.InvalidTransitionException;
import com.smarthome.exception.SmartHomeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Global exception handler implementing RFC 9457 Problem Details for all error responses.
 *
 * <p>All exceptions are caught here to ensure:
 * 1. Consistent error response format across all endpoints.
 * 2. No leaked implementation details (stack traces, SQL, class names).
 * 3. Full exception details logged server-side for debugging.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_URI = "https://smarthome.example.com/problems/";

    @ExceptionHandler(DeviceNotFoundException.class)
    public ProblemDetail handleDeviceNotFound(DeviceNotFoundException ex) {
        log.warn("Device not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create(BASE_URI + "device-not-found"));
        problem.setTitle("Device not found");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(InvalidTransitionException.class)
    public ProblemDetail handleInvalidTransition(InvalidTransitionException ex) {
        log.warn("Invalid transition: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create(BASE_URI + "invalid-transition"));
        problem.setTitle("Invalid state transition");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(SmartHomeException.class)
    public ProblemDetail handleSmartHomeException(SmartHomeException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setType(URI.create(BASE_URI + "business-rule-violation"));
        problem.setTitle("Business rule violation");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(BASE_URI + "invalid-argument"));
        problem.setTitle("Invalid argument");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .reduce("", (a, b) -> a.isEmpty() ? b : a + "; " + b);
        log.warn("Validation error: {}", detail);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(BASE_URI + "validation-error"));
        problem.setTitle("Validation error");
        problem.setDetail(detail);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        // Log full stack trace server-side but never expose to client
        log.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create(BASE_URI + "internal-error"));
        problem.setTitle("Internal server error");
        problem.setDetail("An unexpected error occurred. Please try again.");
        return problem;
    }
}

package com.alten.ecommerce.config.exceptions;

import com.alten.ecommerce.models.dtos.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    /**
     * Handle access error exceptions
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                "You do not have permission to access this resource",
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }


    /**
     * Handle generic runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("Runtime exception occurred", ex);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected exception occurred", ex);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                "Internal Server Error",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // Handle validation from @Validated at service layer
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> validationErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                validationErrors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(
            UserNotFoundException ex, HttpServletRequest request) {

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI(),
                null // No field-level validation errors
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

}
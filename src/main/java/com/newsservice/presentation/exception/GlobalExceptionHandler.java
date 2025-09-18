package com.newsservice.presentation.exception;

import com.newsservice.domain.service.NewsSearchService;
import com.newsservice.infrastructure.external.NewsApiClient;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Global exception handler for the News Search Microservice
 * Provides consistent error responses with HATEOAS links
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EntityModel<ErrorResponse>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "Request validation failed",
                errors,
                LocalDateTime.now()
        );

        EntityModel<ErrorResponse> entityModel = EntityModel.of(errorResponse);
        addErrorLinks(entityModel);

        return ResponseEntity.badRequest().body(entityModel);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<EntityModel<ErrorResponse>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        ErrorResponse errorResponse = new ErrorResponse(
                "CONSTRAINT_VIOLATION",
                "Request constraint violation",
                errors,
                LocalDateTime.now()
        );

        EntityModel<ErrorResponse> entityModel = EntityModel.of(errorResponse);
        addErrorLinks(entityModel);

        return ResponseEntity.badRequest().body(entityModel);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<EntityModel<ErrorResponse>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        
        Map<String, String> errors = Map.of(
                ex.getName(), 
                String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                        ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName())
        );

        ErrorResponse errorResponse = new ErrorResponse(
                "TYPE_MISMATCH",
                "Parameter type mismatch",
                errors,
                LocalDateTime.now()
        );

        EntityModel<ErrorResponse> entityModel = EntityModel.of(errorResponse);
        addErrorLinks(entityModel);

        return ResponseEntity.badRequest().body(entityModel);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<EntityModel<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                Map.of("error", ex.getMessage()),
                LocalDateTime.now()
        );

        EntityModel<ErrorResponse> entityModel = EntityModel.of(errorResponse);
        addErrorLinks(entityModel);

        return ResponseEntity.badRequest().body(entityModel);
    }

    @ExceptionHandler(NewsApiClient.NewsApiException.class)
    public ResponseEntity<EntityModel<ErrorResponse>> handleNewsApiException(
            NewsApiClient.NewsApiException ex) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "NEWS_API_ERROR",
                "External news service error",
                Map.of("error", ex.getMessage()),
                LocalDateTime.now()
        );

        EntityModel<ErrorResponse> entityModel = EntityModel.of(errorResponse);
        addErrorLinks(entityModel);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(entityModel);
    }

    @ExceptionHandler(NewsSearchService.NewsSearchException.class)
    public ResponseEntity<EntityModel<ErrorResponse>> handleNewsSearchException(
            NewsSearchService.NewsSearchException ex) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "NEWS_SEARCH_ERROR",
                "News search operation failed",
                Map.of("error", ex.getMessage()),
                LocalDateTime.now()
        );

        EntityModel<ErrorResponse> entityModel = EntityModel.of(errorResponse);
        addErrorLinks(entityModel);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(entityModel);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<EntityModel<ErrorResponse>> handleGenericException(Exception ex) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                Map.of("error", "Internal server error. Please try again later."),
                LocalDateTime.now()
        );

        EntityModel<ErrorResponse> entityModel = EntityModel.of(errorResponse);
        addErrorLinks(entityModel);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(entityModel);
    }

    /**
     * Add helpful HATEOAS links to error responses
     */
    private void addErrorLinks(EntityModel<ErrorResponse> entityModel) {
        try {
            entityModel.add(linkTo(methodOn(com.newsservice.presentation.controller.NewsSearchController.class)
                    .getApiInfo()).withRel("api-info"));
            entityModel.add(linkTo(methodOn(com.newsservice.presentation.controller.NewsSearchController.class)
                    .getServiceHealth()).withRel("health"));
        } catch (Exception e) {
            // Ignore link creation errors
        }
    }

    /**
     * Standard error response structure
     */
    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private Map<String, String> details;
        private LocalDateTime timestamp;

        public ErrorResponse() {}

        public ErrorResponse(String errorCode, String message, Map<String, String> details, LocalDateTime timestamp) {
            this.errorCode = errorCode;
            this.message = message;
            this.details = details;
            this.timestamp = timestamp;
        }

        // Getters and setters
        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, String> getDetails() {
            return details;
        }

        public void setDetails(Map<String, String> details) {
            this.details = details;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}

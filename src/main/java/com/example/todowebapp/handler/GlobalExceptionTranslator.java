package com.example.todowebapp.handler;

import com.example.todowebapp.exceptions.ApiException;
import jakarta.persistence.EntityNotFoundException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionTranslator {

    @ExceptionHandler(value = {ApiException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO apiException(ApiException apiException) {
        log.error("API exception occurred: {}", apiException.getMessage(), apiException);
        return new ErrorDTO(HttpStatus.BAD_REQUEST, apiException.getMessage());
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleIllegalArgumentException(IllegalArgumentException exception) {
        log.error("Illegal argument exception occurred: {}", exception.getMessage(), exception);
        return new ErrorDTO(HttpStatus.BAD_REQUEST, "Invalid argument: " + exception.getMessage());
    }

    @ExceptionHandler(value = {NullPointerException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDTO handleNullPointerException(NullPointerException exception) {
        log.error("Null pointer exception occurred: {}", exception.getMessage(), exception);
        return new ErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR, "A null pointer exception occurred. Please contact support.");
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorDTO handleAccessDeniedException(AccessDeniedException exception) {
        log.error("Access denied: {}", exception.getMessage(), exception);
        return new ErrorDTO(HttpStatus.FORBIDDEN, "Access denied: " + exception.getMessage());
    }

    @ExceptionHandler(value = {EntityNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO handleEntityNotFoundException(EntityNotFoundException exception) {
        log.error("Entity not found: {}", exception.getMessage(), exception);
        return new ErrorDTO(HttpStatus.NOT_FOUND, "Resource not found: " + exception.getMessage());
    }

    @ExceptionHandler(value = {UnsupportedOperationException.class})
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ErrorDTO handleUnsupportedOperationException(UnsupportedOperationException exception) {
        log.error("Unsupported operation: {}", exception.getMessage(), exception);
        return new ErrorDTO(HttpStatus.NOT_IMPLEMENTED, "This operation is not supported: " + exception.getMessage());
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDTO handleGenericException(Exception exception) {
        log.error("Unexpected exception occurred: {}", exception.getMessage(), exception);
        return new ErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public static final class ErrorDTO {
        private HttpStatus httpStatus;
        private String msg;
    }
}

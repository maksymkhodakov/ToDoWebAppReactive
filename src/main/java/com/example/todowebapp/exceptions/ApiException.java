package com.example.todowebapp.exceptions;

public class ApiException extends RuntimeException {
    public ApiException(ErrorCode errorCode) {
        super(errorCode.getData());
    }

    public ApiException(String message) {
        super(message);
    }
}

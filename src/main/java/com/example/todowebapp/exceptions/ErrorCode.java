package com.example.todowebapp.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    TODO_TASK_NOT_FOUND("Todo task not found"),
    USER_ALREADY_EXISTS("User already exists"),
    USER_NOT_FOUND("User not found"),
    USER_CANNOT_DELETE_ANOTHER_USER_TODO("User cannot delete another user todo"),
    YOU_CANNOT_CREATE_AN_ADMIN_USER("You cannot create a user with admin role"),
    ROLE_NOT_FOUND("User role not found");
    private final String data;
}

package com.example.todowebapp.service;

import com.example.todowebapp.domain.dto.TodoDTO;
import com.example.todowebapp.security.AuthenticationUserDetails;

import java.util.List;
import java.util.Set;

public interface TodoService {
    List<TodoDTO> getTodos(AuthenticationUserDetails userDetails);
    TodoDTO createTodo(TodoDTO todo, AuthenticationUserDetails userDetails);
    TodoDTO updateTodo(TodoDTO todo, AuthenticationUserDetails userDetails);
    void deleteTodos(Set<Long> ids, AuthenticationUserDetails userDetails);
}

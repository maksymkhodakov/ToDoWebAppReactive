package com.example.todowebapp.service;

import com.example.todowebapp.domain.dto.TodoDTO;
import com.example.todowebapp.security.AuthenticationUserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface TodoService {
    Flux<TodoDTO> getTodos(AuthenticationUserDetails userDetails);
    Mono<TodoDTO> createTodo(TodoDTO todo, AuthenticationUserDetails userDetails);
    Mono<TodoDTO> updateTodo(TodoDTO todo, AuthenticationUserDetails userDetails);
    Flux<TodoDTO> deleteTodos(Set<Long> ids, AuthenticationUserDetails userDetails);
}

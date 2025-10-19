package com.example.todowebapp.api;

import com.example.todowebapp.domain.dto.IdDTO;
import com.example.todowebapp.domain.dto.TodoDTO;
import com.example.todowebapp.domain.enumerated.UserPrivilege;
import com.example.todowebapp.security.AuthenticationUserDetails;
import com.example.todowebapp.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PreAuthorize(UserPrivilege.Authority.VIEW_TODOS)
    @GetMapping("/todos")
    @Operation(description = "Retrieve related todo task(s)")
    public Flux<TodoDTO> getTodos(@AuthenticationPrincipal AuthenticationUserDetails principal) {
        return todoService.getTodos(principal);
    }

    @PreAuthorize(UserPrivilege.Authority.CREATE_TODOS)
    @PostMapping("/todo/create")
    @Operation(description = "Create todo task")
    public Mono<TodoDTO> createTodo(@RequestBody @Valid Mono<TodoDTO> body,
                                    @AuthenticationPrincipal AuthenticationUserDetails principal) {
        return body.flatMap(dto -> todoService.createTodo(dto, principal));
    }

    @PreAuthorize(UserPrivilege.Authority.UPDATE_TODOS)
    @PutMapping("/todo/update")
    @Operation(description = "Update todo task")
    public Mono<TodoDTO> updateTodo(@RequestBody @Valid Mono<TodoDTO> body,
                                    @AuthenticationPrincipal AuthenticationUserDetails principal) {
        return body.flatMap(dto -> todoService.updateTodo(dto, principal));
    }

    @PreAuthorize(UserPrivilege.Authority.DELETE_TODOS)
    @DeleteMapping("/todo/delete")
    @Operation(description = "Delete todo task(s)")
    public Flux<TodoDTO> deleteTodos(@RequestBody @Valid Mono<IdDTO> body,
                                     @AuthenticationPrincipal AuthenticationUserDetails userDetails) {
        return body.flatMapMany(dto -> todoService.deleteTodos(dto.getIds(), userDetails));
    }
}

package com.example.todowebapp.api;

import com.example.todowebapp.domain.dto.TodoDTO;
import com.example.todowebapp.domain.enumerated.UserPrivilege;
import com.example.todowebapp.security.AuthenticationUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.todowebapp.service.TodoService;

import java.util.List;
import java.util.Set;


@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;

    @PreAuthorize(UserPrivilege.Authority.VIEW_TODOS)
    @GetMapping("/todos")
    @Operation(description = "Retrieve related todo task(s)")
    public ResponseEntity<List<TodoDTO>> getTodos(@AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {
        return ResponseEntity.ok(todoService.getTodos(authenticationUserDetails));
    }

    @PreAuthorize(UserPrivilege.Authority.CREATE_TODOS)
    @PostMapping("/todo/create")
    @Operation(description = "Create todo task")
    public ResponseEntity<TodoDTO> createTodo(@RequestBody @Valid final TodoDTO todo,
                                              @AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {
        return ResponseEntity.ok(todoService.createTodo(todo, authenticationUserDetails));
    }

    @PreAuthorize(UserPrivilege.Authority.UPDATE_TODOS)
    @PutMapping("/todo/update")
    @Operation(description = "Update todo task")
    public ResponseEntity<TodoDTO> updateTodo(@RequestBody @Valid final TodoDTO todo,
                                              @AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {
        return ResponseEntity.ok(todoService.updateTodo(todo, authenticationUserDetails));
    }

    @PreAuthorize(UserPrivilege.Authority.DELETE_TODOS)
    @DeleteMapping("/todo/delete")
    @Operation(description = "Delete todo task(s)")
    public ResponseEntity<Void> deleteTodos(@RequestParam final Set<Long> ids,
                                            @AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {
        todoService.deleteTodos(ids, authenticationUserDetails);
        return ResponseEntity.ok().build();
    }
}

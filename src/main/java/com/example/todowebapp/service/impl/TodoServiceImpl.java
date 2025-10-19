package com.example.todowebapp.service.impl;

import com.example.todowebapp.domain.dto.TodoDTO;
import com.example.todowebapp.domain.entity.Todo;
import com.example.todowebapp.domain.entity.User;
import com.example.todowebapp.exceptions.ApiException;
import com.example.todowebapp.exceptions.ErrorCode;
import com.example.todowebapp.repository.TodoRepository;
import com.example.todowebapp.repository.UserRepository;
import com.example.todowebapp.security.AuthenticationUserDetails;
import com.example.todowebapp.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;

    /**
     * Retrieve all todos for current user.
     */
    @Override
    public Flux<TodoDTO> getTodos(final AuthenticationUserDetails userDetails) {
        final Long userId = userDetails.getUserId();
        return todoRepository.findAllByUserId(userId)
                .map(this::toDto);
    }

    /**
     * Create a todos for current user.
     */
    @Override
    @Transactional
    public Mono<TodoDTO> createTodo(final TodoDTO dto,
                                    final AuthenticationUserDetails userDetails) {

        final Long userId = userDetails.getUserId();

        // ensure user exists (and get any needed flags)
        Mono<User> userMono = userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ApiException(ErrorCode.USER_NOT_FOUND)));

        return userMono.flatMap(u -> {
            Todo entity = Todo.builder()
                    .description(dto.getDescription())
                    .dueDate(dto.getDueDate())
                    .checkMark(Boolean.TRUE.equals(dto.isCheckMark()))
                    .completionDate(dto.getCompletionDate())
                    .userId(userId)   // set FK explicitly
                    .build();
            return todoRepository.save(entity).map(this::toDto);
        });
    }

    /**
     * Update a todos. (Optionally verify ownership.)
     */
    @Override
    @Transactional
    public Mono<TodoDTO> updateTodo(final TodoDTO dto,
                                    final AuthenticationUserDetails userDetails) {
        if (dto.getId() == null) {
            return Mono.error(new ApiException(ErrorCode.TODO_TASK_NOT_FOUND));
        }
        final Long userId = userDetails.getUserId();

        return todoRepository.findById(dto.getId())
                .switchIfEmpty(Mono.error(new ApiException(ErrorCode.TODO_TASK_NOT_FOUND)))
                // verify that this todos belongs to the current user
                .flatMap(existing -> {
                    if (existing.getUserId() == null || !existing.getUserId().equals(userId)) {
                        return Mono.error(new ApiException(ErrorCode.USER_CANNOT_UPDATE_ANOTHER_USER_TODO));
                    }
                    existing.setDescription(dto.getDescription());
                    existing.setCheckMark(dto.isCheckMark());
                    existing.setDueDate(dto.getDueDate());
                    existing.setCompletionDate(dto.getCompletionDate());
                    return todoRepository.save(existing).map(this::toDto);
                });
    }

    /**
     * Delete multiple todos.
     */
    @Override
    @Transactional
    public Flux<TodoDTO> deleteTodos(final Set<Long> ids,
                                     final AuthenticationUserDetails user) {
        Long userId = user.getUserId();

        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }

        return todoRepository.findAllByIdInAndUserId(ids, userId)
                .collectList()
                .flatMapMany(found -> {
                    if (found.isEmpty()) {
                        return Flux.error(new ApiException("No todos found for given IDs"));
                    }

                    // Convert to DTOs *before* deletion
                    final List<TodoDTO> deletedDTOs = found.stream()
                            .map(this::toDto)
                            .toList();

                    // Delete all, then emit deleted dto
                    return todoRepository.deleteAll(found)
                            .thenMany(Flux.fromIterable(deletedDTOs));
                });
    }

    private TodoDTO toDto(final Todo t) {
        return TodoDTO.builder()
                .id(t.getId())
                .description(t.getDescription())
                .dueDate(t.getDueDate())
                .checkMark(t.isCheckMark())
                .completionDate(t.getCompletionDate())
                .build();
    }
}

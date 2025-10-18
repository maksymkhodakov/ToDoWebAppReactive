package com.example.todowebapp.service.impl;

import com.example.todowebapp.domain.dto.TodoDTO;
import com.example.todowebapp.domain.entity.Todo;
import com.example.todowebapp.domain.entity.User;
import com.example.todowebapp.exceptions.ApiException;
import com.example.todowebapp.exceptions.ErrorCode;
import com.example.todowebapp.repository.UserRepository;
import com.example.todowebapp.security.AuthenticationUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.todowebapp.repository.TodoRepository;
import com.example.todowebapp.service.TodoService;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;


    /**
     * this method is for retrieving all todos related to the user
     * @return  list of dto
     */
    @Override
    public List<TodoDTO> getTodos(final AuthenticationUserDetails userDetails) {
        return todoRepository.findAllByUserId(userDetails.getUserId())
                .stream()
                .map(this::getTodoDTO)
                .toList();
    }


    /**
     * This method is used to create an object
     * @return      saved dto
     */
    @Override
    @Transactional
    public TodoDTO createTodo(final TodoDTO todo,
                              final AuthenticationUserDetails userDetails) {
        final User user = getUser(userDetails);

        final Todo todoToSave = Todo.builder()
                .description(todo.getDescription())
                .dueDate(todo.getDueDate())
                .checkMark(todo.isCheckMark())
                .completionDate(todo.getCompletionDate())
                .build();

        final Todo savedTodo = todoRepository.save(todoToSave);

        user.addTodo(savedTodo);

        userRepository.save(user);

        return getTodoDTO(savedTodo);
    }


    /**
     * This method is used for updating task information
     * @return updated task object
     */
    @Override
    @Transactional
    public TodoDTO updateTodo(final TodoDTO todo,
                              final AuthenticationUserDetails userDetails) {
        if (todo.getId() == null) {
            throw new ApiException(ErrorCode.TODO_TASK_NOT_FOUND);
        }

        final Todo todoInDb = todoRepository.findById(todo.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.TODO_TASK_NOT_FOUND));

        todoInDb.setDescription(todo.getDescription());
        todoInDb.setCheckMark(todo.isCheckMark());
        todoInDb.setDueDate(todo.getDueDate());
        todoInDb.setCompletionDate(todo.getCompletionDate());

        return getTodoDTO(todoRepository.save(todoInDb));
    }


    private TodoDTO getTodoDTO(final Todo todo) {
        return TodoDTO.builder()
                .id(todo.getId())
                .description(todo.getDescription())
                .dueDate(todo.getDueDate())
                .checkMark(todo.isCheckMark())
                .completionDate(todo.getCompletionDate())
                .build();
    }


    /**
     * This method is used for deletion of objects
     *
     * @param ids                       of tasks
     * @param userDetails               contains authenticated user info
     */
    @Override
    @Transactional
    public void deleteTodos(final Set<Long> ids,
                            final AuthenticationUserDetails userDetails) {
        final User user = getUser(userDetails);

        final Set<Long> userTodos = user.getTodos() == null ?
                new HashSet<>() :
                user.getTodos()
                        .stream()
                        .map(Todo::getId)
                        .collect(Collectors.toSet());

        final List<Todo> todos = todoRepository.findAllById(ids);

        if (todos.size() != ids.size()) {
            throw new ApiException(ErrorCode.TODO_TASK_NOT_FOUND);
        }

        if (!userTodos.containsAll(ids)) {
            throw new ApiException(ErrorCode.USER_CANNOT_DELETE_ANOTHER_USER_TODO);
        }

        todoRepository.deleteAllByIdInBatch(ids);
    }

    private User getUser(final AuthenticationUserDetails userDetails) {
        return userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }
}

package com.example.todowebapp.service;

import com.example.todowebapp.domain.dto.TodoDTO;
import com.example.todowebapp.domain.entity.Todo;
import com.example.todowebapp.domain.entity.User;
import com.example.todowebapp.domain.enumerated.UserRole;
import com.example.todowebapp.exceptions.ApiException;
import com.example.todowebapp.exceptions.ErrorCode;
import com.example.todowebapp.repository.TodoRepository;
import com.example.todowebapp.repository.UserRepository;
import com.example.todowebapp.security.AuthenticationUserDetails;
import com.example.todowebapp.service.impl.TodoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoServiceImpl todoService;

    private AuthenticationUserDetails userDetails;
    private User user;
    private Todo todo;

    @BeforeEach
    void setUp() {
        // Створюємо principal через builder класу AuthenticationUserDetails
        userDetails = AuthenticationUserDetails.authBuilder()
                .username("testUser")
                .password("password")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(Collections.emptyList())
                .userId(1L)
                .userRole(UserRole.ROLE_BASIC_USER)
                .system(false)
                .build();

        user = User.builder()
                .id(1L)
                .todos(new ArrayList<>())
                .build();

        todo = Todo.builder()
                .id(100L)
                .description("Test description")
                .dueDate(LocalDate.now().plusDays(1))
                .checkMark(false)
                .completionDate(null)
                .build();
    }

    @Test
    void testGetTodos_ReturnsTodoDTOList() {
        // given
        when(todoRepository.findAllByUserId(1L)).thenReturn(List.of(todo));

        // when
        List<TodoDTO> result = todoService.getTodos(userDetails);

        // then
        assertThat(result).hasSize(1);
        TodoDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(todo.getId());
        assertThat(dto.getDescription()).isEqualTo(todo.getDescription());
        assertThat(dto.getDueDate()).isEqualTo(todo.getDueDate());
        assertThat(dto.isCheckMark()).isEqualTo(todo.isCheckMark());
        assertThat(dto.getCompletionDate()).isEqualTo(todo.getCompletionDate());
    }

    @Test
    void testCreateTodo_Success() {
        // given
        TodoDTO todoDTO = TodoDTO.builder()
                .description("New todo")
                .dueDate(LocalDate.now().plusDays(2))
                .checkMark(false)
                .completionDate(null)
                .build();

        // Імітуємо, що користувач знайдений
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Імітуємо збереження Todos, присвоюючи новий id
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> {
            Todo t = invocation.getArgument(0);
            t.setId(101L);
            return t;
        });
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        TodoDTO result = todoService.createTodo(todoDTO, userDetails);

        // then
        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getDescription()).isEqualTo("New todo");
        assertThat(result.getDueDate()).isEqualTo(todoDTO.getDueDate());
        assertThat(result.isCheckMark()).isFalse();
        // Перевірка, що todos було додано до користувача
        assertThat(user.getTodos()).extracting(Todo::getId).contains(101L);
    }

    @Test
    void testCreateTodo_UserNotFound_ThrowsException() {
        // given
        TodoDTO todoDTO = TodoDTO.builder().description("New todo").build();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when/then
        final ApiException exception = assertThrows(ApiException.class, () -> todoService.createTodo(todoDTO, userDetails));
        assertEquals(exception.getMessage(), ErrorCode.USER_NOT_FOUND.getData());
    }

    @Test
    void testUpdateTodo_Success() {
        // given
        TodoDTO updateDTO = TodoDTO.builder()
                .id(100L)
                .description("Updated description")
                .dueDate(LocalDate.now().plusDays(3))
                .checkMark(true)
                .completionDate(LocalDate.now())
                .build();

        when(todoRepository.findById(100L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        TodoDTO result = todoService.updateTodo(updateDTO, userDetails);

        // then
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getDueDate()).isEqualTo(updateDTO.getDueDate());
        assertThat(result.isCheckMark()).isTrue();
        assertThat(result.getCompletionDate()).isEqualTo(updateDTO.getCompletionDate());
    }

    @Test
    void testUpdateTodo_NullId_ThrowsException() {
        // given
        TodoDTO updateDTO = TodoDTO.builder()
                .description("Updated description")
                .build();

        // when/then
        final ApiException exception = assertThrows(ApiException.class, () -> todoService.updateTodo(updateDTO, userDetails));
        assertEquals(exception.getMessage(), ErrorCode.TODO_TASK_NOT_FOUND.getData());
    }

    @Test
    void testUpdateTodo_TodoNotFound_ThrowsException() {
        // given
        TodoDTO updateDTO = TodoDTO.builder()
                .id(999L)
                .description("Updated description")
                .build();
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        // when/then
        final ApiException exception = assertThrows(ApiException.class, () -> todoService.updateTodo(updateDTO, userDetails));
        assertEquals(exception.getMessage(), ErrorCode.TODO_TASK_NOT_FOUND.getData());
    }

    @Test
    void testDeleteTodos_Success() {
        // given
        // Додаємо todos користувачу
        user.getTodos().add(todo);
        Set<Long> idsToDelete = Set.of(100L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(todoRepository.findAllById(idsToDelete)).thenReturn(List.of(todo));

        // when
        todoService.deleteTodos(idsToDelete, userDetails);

        // then
        verify(todoRepository, times(1)).deleteAllByIdInBatch(idsToDelete);
    }

    @Test
    void testDeleteTodos_NotAllTodosFound_ThrowsException() {
        // given
        Set<Long> idsToDelete = Set.of(100L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(todoRepository.findAllById(idsToDelete)).thenReturn(Collections.emptyList());

        // when/then
        final ApiException exception = assertThrows(ApiException.class, () -> todoService.deleteTodos(idsToDelete, userDetails));
        assertEquals(exception.getMessage(), ErrorCode.TODO_TASK_NOT_FOUND.getData());
    }

    @Test
    void testDeleteTodos_AttemptToDeleteAnotherUsersTodo_ThrowsException() {
        // given
        // Користувач не має todos з id 200L, але репозиторій повертає таке todos,
        // яке не належить цьому користувачу
        Todo otherUserTodo = Todo.builder()
                .id(200L)
                .description("Other user's todo")
                .build();
        Set<Long> idsToDelete = Set.of(200L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(todoRepository.findAllById(idsToDelete)).thenReturn(List.of(otherUserTodo));

        final ApiException exception = assertThrows(ApiException.class, () -> todoService.deleteTodos(idsToDelete, userDetails));
        assertEquals(exception.getMessage(), ErrorCode.USER_CANNOT_DELETE_ANOTHER_USER_TODO.getData());
    }
}

package com.example.todowebapp.api;

import com.example.todowebapp.domain.dto.IdDTO;
import com.example.todowebapp.domain.dto.LoginData;
import com.example.todowebapp.domain.dto.LoginResponseDTO;
import com.example.todowebapp.domain.dto.TodoDTO;
import com.example.todowebapp.domain.entity.Role;
import com.example.todowebapp.domain.entity.Todo;
import com.example.todowebapp.domain.entity.User;
import com.example.todowebapp.domain.enumerated.UserRole;
import com.example.todowebapp.repository.RoleRepository;
import com.example.todowebapp.repository.TodoRepository;
import com.example.todowebapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for TodoController API.
 * Tests the complete flow including authentication, authorization, and CRUD operations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@DisplayName("TodoController E2E Tests")
class TodoControllerE2ETest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private Long userId;
    private Long roleId;
    private static final String TEST_EMAIL = "testuser@example.com";
    private static final String TEST_PASSWORD = "testpass123";
    private static final String API_BASE = "/api";

    @BeforeEach
    void setUp() {
        // Clean up data from previous tests
        todoRepository.deleteAll().block();
        userRepository.deleteAll().block();

        // Create or retrieve ROLE_BASIC_USER role
        Role userRole = roleRepository.findByUserRole(UserRole.ROLE_BASIC_USER)
                .switchIfEmpty(roleRepository.save(Role.builder()
                        .userRole(UserRole.ROLE_BASIC_USER)
                        .build()))
                .block();
        assertThat(userRole).isNotNull();
        roleId = userRole.getId();

        // Create test user
        User testUser = User.builder()
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .name("Test")
                .lastName("User")
                .roleId(roleId)
                .system(false)
                .build();

        User savedUser = userRepository.save(testUser).block();
        assertThat(savedUser).isNotNull();
        userId = savedUser.getId();

        // Authenticate to get JWT token
        authToken = authenticateAndGetToken();
    }

    /**
     * Helper method to authenticate and retrieve JWT token
     */
    private String authenticateAndGetToken() {
        LoginData loginData = LoginData.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();

        LoginResponseDTO response = webTestClient.post()
                .uri(API_BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginData)
                .exchange()
                .expectStatus().isOk()
                .returnResult(LoginResponseDTO.class)
                .getResponseBody()
                .blockFirst();

        assertThat(response).isNotNull();
        return response.getToken();
    }

    @Nested
    @DisplayName("GET /api/todos - Retrieve Todos")
    class GetTodosTests {

        @Test
        @DisplayName("Should retrieve empty list when user has no todos")
        void shouldRetrieveEmptyListWhenNoTodos() {
            webTestClient.get()
                    .uri(API_BASE + "/todos")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(TodoDTO.class)
                    .hasSize(0);
        }

        @Test
        @DisplayName("Should retrieve all todos for authenticated user")
        void shouldRetrieveAllTodosForAuthenticatedUser() {
            Todo todo1 = Todo.builder()
                    .description("First task")
                    .dueDate(LocalDate.now().plusDays(1))
                    .checkMark(false)
                    .userId(userId)
                    .build();

            Todo todo2 = Todo.builder()
                    .description("Second task")
                    .dueDate(LocalDate.now().plusDays(2))
                    .checkMark(true)
                    .completionDate(LocalDate.now())
                    .userId(userId)
                    .build();

            todoRepository.save(todo1).block();
            todoRepository.save(todo2).block();

            webTestClient.get()
                    .uri(API_BASE + "/todos")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(TodoDTO.class)
                    .hasSize(2)
                    .consumeWith(response -> {
                        var todos = response.getResponseBody();
                        assertThat(todos)
                                .extracting(TodoDTO::getDescription)
                                .contains("First task", "Second task");
                    });
        }

        @Test
        @DisplayName("Should return 401 when authorization header is missing")
        void shouldReturn401WhenAuthorizationHeaderMissing() {
            webTestClient.get()
                    .uri(API_BASE + "/todos")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Should return 401 when token is invalid")
        void shouldReturn401WhenTokenIsInvalid() {
            webTestClient.get()
                    .uri(API_BASE + "/todos")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }

    @Nested
    @DisplayName("POST /api/todo/create - Create Todo")
    class CreateTodoTests {

        @Test
        @DisplayName("Should create a new todo successfully")
        void shouldCreateNewTodoSuccessfully() {
            TodoDTO newTodo = TodoDTO.builder()
                    .description("New task")
                    .dueDate(LocalDate.now().plusDays(5))
                    .checkMark(false)
                    .build();

            webTestClient.post()
                    .uri(API_BASE + "/todo/create")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(newTodo)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(TodoDTO.class)
                    .consumeWith(response -> {
                        TodoDTO createdTodo = response.getResponseBody();
                        assertThat(createdTodo)
                                .isNotNull()
                                .satisfies(todo -> {
                                    assertThat(todo.getId()).isNotNull();
                                    assertThat(todo.getDescription()).isEqualTo("New task");
                                    assertThat(todo.getDueDate()).isEqualTo(LocalDate.now().plusDays(5));
                                    assertThat(todo.isCheckMark()).isFalse();
                                });
                    });
        }

        @Test
        @DisplayName("Should create todo with completion date when marked as completed")
        void shouldCreateTodoWithCompletionDateWhenMarkedAsCompleted() {
            LocalDate completionDate = LocalDate.now();
            TodoDTO newTodo = TodoDTO.builder()
                    .description("Completed task")
                    .dueDate(LocalDate.now().minusDays(1))
                    .checkMark(true)
                    .completionDate(completionDate)
                    .build();

            webTestClient.post()
                    .uri(API_BASE + "/todo/create")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(newTodo)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(TodoDTO.class)
                    .consumeWith(response -> {
                        TodoDTO createdTodo = response.getResponseBody();
                        assertThat(createdTodo)
                                .isNotNull()
                                .satisfies(todo -> {
                                    assertThat(todo.isCheckMark()).isTrue();
                                    assertThat(todo.getCompletionDate()).isEqualTo(completionDate);
                                });
                    });
        }

        @Test
        @DisplayName("Should fail validation when description is missing")
        void shouldFailValidationWhenDescriptionMissing() {
            TodoDTO invalidTodo = TodoDTO.builder()
                    .dueDate(LocalDate.now().plusDays(5))
                    .checkMark(false)
                    .build();

            webTestClient.post()
                    .uri(API_BASE + "/todo/create")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(invalidTodo)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should fail validation when due date is missing")
        void shouldFailValidationWhenDueDateMissing() {
            TodoDTO invalidTodo = TodoDTO.builder()
                    .description("Task without due date")
                    .checkMark(false)
                    .build();

            webTestClient.post()
                    .uri(API_BASE + "/todo/create")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(invalidTodo)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should return 401 when authorization header is missing")
        void shouldReturn401WhenAuthorizationHeaderMissing() {
            TodoDTO newTodo = TodoDTO.builder()
                    .description("New task")
                    .dueDate(LocalDate.now().plusDays(5))
                    .checkMark(false)
                    .build();

            webTestClient.post()
                    .uri(API_BASE + "/todo/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(newTodo)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Should return 401 when token is invalid")
        void shouldReturn401WhenTokenIsInvalid() {
            TodoDTO newTodo = TodoDTO.builder()
                    .description("New task")
                    .dueDate(LocalDate.now().plusDays(5))
                    .checkMark(false)
                    .build();

            webTestClient.post()
                    .uri(API_BASE + "/todo/create")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(newTodo)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }

    @Nested
    @DisplayName("PUT /api/todo/update - Update Todo")
    class UpdateTodoTests {

        private Long todoId;

        @BeforeEach
        void setUpTodos() {
            Todo todo = Todo.builder()
                    .description("Original task")
                    .dueDate(LocalDate.now().plusDays(1))
                    .checkMark(false)
                    .userId(userId)
                    .build();
            Todo savedTodo = todoRepository.save(todo).block();
            assertThat(savedTodo).isNotNull();
            todoId = savedTodo.getId();
        }

        @Test
        @DisplayName("Should update todo successfully")
        void shouldUpdateTodoSuccessfully() {
            TodoDTO updatedTodo = TodoDTO.builder()
                    .id(todoId)
                    .description("Updated task")
                    .dueDate(LocalDate.now().plusDays(10))
                    .checkMark(false)
                    .build();

            webTestClient.put()
                    .uri(API_BASE + "/todo/update")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updatedTodo)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(TodoDTO.class)
                    .consumeWith(response -> {
                        TodoDTO result = response.getResponseBody();
                        assertThat(result)
                                .isNotNull()
                                .satisfies(todo -> {
                                    assertThat(todo.getId()).isEqualTo(todoId);
                                    assertThat(todo.getDescription()).isEqualTo("Updated task");
                                    assertThat(todo.getDueDate()).isEqualTo(LocalDate.now().plusDays(10));
                                });
                    });
        }

        @Test
        @DisplayName("Should mark todo as complete with completion date")
        void shouldMarkTodoAsCompleteWithCompletionDate() {
            LocalDate completionDate = LocalDate.now();
            TodoDTO completedTodo = TodoDTO.builder()
                    .id(todoId)
                    .description("Original task")
                    .dueDate(LocalDate.now().plusDays(1))
                    .checkMark(true)
                    .completionDate(completionDate)
                    .build();

            webTestClient.put()
                    .uri(API_BASE + "/todo/update")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(completedTodo)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(TodoDTO.class)
                    .consumeWith(response -> {
                        TodoDTO result = response.getResponseBody();
                        assertThat(result)
                                .isNotNull()
                                .satisfies(todo -> {
                                    assertThat(todo.isCheckMark()).isTrue();
                                    assertThat(todo.getCompletionDate()).isEqualTo(completionDate);
                                });
                    });
        }

        @Test
        @DisplayName("Should fail when todo id is missing")
        void shouldFailWhenTodoIdMissing() {
            TodoDTO updateTodo = TodoDTO.builder()
                    .description("Updated task")
                    .dueDate(LocalDate.now().plusDays(10))
                    .checkMark(false)
                    .build();

            webTestClient.put()
                    .uri(API_BASE + "/todo/update")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updateTodo)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should fail when todo does not exist")
        void shouldFailWhenTodoDoesNotExist() {
            TodoDTO updateTodo = TodoDTO.builder()
                    .id(99999L)
                    .description("Updated task")
                    .dueDate(LocalDate.now().plusDays(10))
                    .checkMark(false)
                    .build();

            webTestClient.put()
                    .uri(API_BASE + "/todo/update")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updateTodo)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should fail when user tries to update another user's todo")
        void shouldFailWhenUserUpdatesAnotherUsersTodo() {
            // Create another user
            User otherUser = User.builder()
                    .email("anotheruser@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .name("Another")
                    .lastName("User")
                    .roleId(roleId)
                    .system(false)
                    .build();
            User savedOtherUser = userRepository.save(otherUser).block();
            assertThat(savedOtherUser).isNotNull();

            Todo otherUserTodo = Todo.builder()
                    .description("Other user's task")
                    .dueDate(LocalDate.now().plusDays(1))
                    .checkMark(false)
                    .userId(savedOtherUser.getId())
                    .build();
            Todo savedOtherTodo = todoRepository.save(otherUserTodo).block();
            assertThat(savedOtherTodo).isNotNull();

            TodoDTO updateTodo = TodoDTO.builder()
                    .id(savedOtherTodo.getId())
                    .description("Hacked task")
                    .dueDate(LocalDate.now().plusDays(10))
                    .checkMark(false)
                    .build();

            webTestClient.put()
                    .uri(API_BASE + "/todo/update")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updateTodo)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should return 401 when authorization header is missing")
        void shouldReturn401WhenAuthorizationHeaderMissing() {
            TodoDTO updateTodo = TodoDTO.builder()
                    .id(todoId)
                    .description("Updated task")
                    .dueDate(LocalDate.now().plusDays(10))
                    .checkMark(false)
                    .build();

            webTestClient.put()
                    .uri(API_BASE + "/todo/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updateTodo)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }

    @Nested
    @DisplayName("DELETE /api/todo/delete - Delete Todos")
    class DeleteTodosTests {

        private Long todoId1;
        private Long todoId2;

        @BeforeEach
        void setUpTodos() {
            Todo todo1 = Todo.builder()
                    .description("Task to delete 1")
                    .dueDate(LocalDate.now().plusDays(1))
                    .checkMark(false)
                    .userId(userId)
                    .build();

            Todo todo2 = Todo.builder()
                    .description("Task to delete 2")
                    .dueDate(LocalDate.now().plusDays(2))
                    .checkMark(false)
                    .userId(userId)
                    .build();

            Todo savedTodo1 = todoRepository.save(todo1).block();
            Todo savedTodo2 = todoRepository.save(todo2).block();

            assertThat(savedTodo1).isNotNull();
            assertThat(savedTodo2).isNotNull();

            todoId1 = savedTodo1.getId();
            todoId2 = savedTodo2.getId();
        }

        @Test
        @DisplayName("Should delete single todo successfully")
        void shouldDeleteSingleTodoSuccessfully() {
            IdDTO deleteRequest = IdDTO.builder()
                    .ids(Set.of(todoId1))
                    .build();

            webTestClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri(API_BASE + "/todo/delete")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(deleteRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(TodoDTO.class)
                    .hasSize(1)
                    .consumeWith(response -> {
                        assertThat(response.getResponseBody())
                                .extracting(TodoDTO::getId)
                                .contains(todoId1);
                    });

            var remainingTodos = todoRepository.findAllByUserId(userId).collectList().block();
            assertThat(remainingTodos).hasSize(1);
        }

        @Test
        @DisplayName("Should delete multiple todos successfully")
        void shouldDeleteMultipleTodosSuccessfully() {
            IdDTO deleteRequest = IdDTO.builder()
                    .ids(Set.of(todoId1, todoId2))
                    .build();

            webTestClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri(API_BASE + "/todo/delete")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(deleteRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(TodoDTO.class)
                    .hasSize(2);

            var remainingTodos = todoRepository.findAllByUserId(userId).collectList().block();
            assertThat(remainingTodos).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when ids set is empty")
        void shouldFailValidationWhenIdsSetIsEmpty() {
            IdDTO deleteRequest = IdDTO.builder()
                    .ids(new HashSet<>())
                    .build();

            webTestClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri(API_BASE + "/todo/delete")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(deleteRequest)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should fail when ids is null")
        void shouldFailWhenIdsIsNull() {
            IdDTO deleteRequest = IdDTO.builder()
                    .build();

            webTestClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri(API_BASE + "/todo/delete")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(deleteRequest)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should not delete todos that don't belong to user")
        void shouldNotDeleteTodosThatDontBelongToUser() {
            User otherUser = User.builder()
                    .email("anotheruser2@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .name("Another")
                    .lastName("User2")
                    .roleId(roleId)
                    .system(false)
                    .build();
            User savedOtherUser = userRepository.save(otherUser).block();
            assertThat(savedOtherUser).isNotNull();

            Todo otherUserTodo = Todo.builder()
                    .description("Other user's task")
                    .dueDate(LocalDate.now().plusDays(1))
                    .checkMark(false)
                    .userId(savedOtherUser.getId())
                    .build();
            Todo savedOtherTodo = todoRepository.save(otherUserTodo).block();
            assertThat(savedOtherTodo).isNotNull();

            IdDTO deleteRequest = IdDTO.builder()
                    .ids(Set.of(savedOtherTodo.getId()))
                    .build();

            webTestClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri(API_BASE + "/todo/delete")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(deleteRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(TodoDTO.class)
                    .hasSize(0);

            var otherUserTodos = todoRepository.findAllByUserId(savedOtherUser.getId()).collectList().block();
            assertThat(otherUserTodos).hasSize(1);
        }

        @Test
        @DisplayName("Should return 401 when authorization header is missing")
        void shouldReturn401WhenAuthorizationHeaderMissing() {
            IdDTO deleteRequest = IdDTO.builder()
                    .ids(Set.of(todoId1))
                    .build();

            webTestClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri(API_BASE + "/todo/delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(deleteRequest)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }
}









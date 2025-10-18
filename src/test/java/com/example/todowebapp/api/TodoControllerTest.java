package com.example.todowebapp.api;

import com.example.todowebapp.domain.dto.TodoDTO;
import com.example.todowebapp.security.AuthenticationUserDetails;
import com.example.todowebapp.service.TodoService;
import com.example.todowebapp.service.impl.JwtServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TodoController.class)
@Import(TodoControllerTest.TestSecurityConfig.class)
class TodoControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    public static class TestSecurityConfig {
        @Bean
        public DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler() {
            return new DefaultMethodSecurityExpressionHandler();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    @MockBean
    private JwtServiceImpl jwtService;


    private AuthenticationUserDetails createPrincipalWithAuthority(String authority) {
        return AuthenticationUserDetails.authBuilder()
                .username("user@example.com")
                .password("password")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(Collections.singleton(new SimpleGrantedAuthority(authority)))
                .userId(1L)
                .userRole(null)
                .system(false)
                .build();
    }

    @Test
    void testGetTodos_Success() throws Exception {
        AuthenticationUserDetails principal = createPrincipalWithAuthority("VIEW_TODOS");

        TodoDTO todoDTO = TodoDTO.builder()
                .id(100L)
                .description("Test todo")
                .dueDate(LocalDate.now())
                .checkMark(false)
                .completionDate(null)
                .build();

        when(todoService.getTodos(any())).thenReturn(List.of(todoDTO));

        mockMvc.perform(get("/api/todos")
                        .with(user(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(100)))
                .andExpect(jsonPath("$[0].description", is("Test todo")));
    }

    @Test
    void testGetTodos_Failure() throws Exception {
        AuthenticationUserDetails principal = createPrincipalWithAuthority("test");

        TodoDTO todoDTO = TodoDTO.builder()
                .id(100L)
                .description("Test todo")
                .dueDate(LocalDate.now())
                .checkMark(false)
                .completionDate(null)
                .build();

        when(todoService.getTodos(any())).thenReturn(List.of(todoDTO));

        mockMvc.perform(get("/api/todos")
                        .with(user(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateTodo_Success() throws Exception {
        AuthenticationUserDetails principal = createPrincipalWithAuthority("CREATE_TODOS");

        TodoDTO inputTodo = TodoDTO.builder()
                .description("New todo")
                .dueDate(LocalDate.now().plusDays(1))
                .checkMark(false)
                .completionDate(null)
                .build();

        TodoDTO outputTodo = TodoDTO.builder()
                .id(101L)
                .description("New todo")
                .dueDate(inputTodo.getDueDate())
                .checkMark(false)
                .completionDate(null)
                .build();

        when(todoService.createTodo(any(TodoDTO.class), any())).thenReturn(outputTodo);

        mockMvc.perform(post("/api/todo/create")
                        .with(user(principal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputTodo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(101)))
                .andExpect(jsonPath("$.description", is("New todo")));
    }

    @Test
    void testCreateTodo_Failure() throws Exception {
        AuthenticationUserDetails principal = createPrincipalWithAuthority("test");

        TodoDTO inputTodo = TodoDTO.builder()
                .description("New todo")
                .dueDate(LocalDate.now().plusDays(1))
                .checkMark(false)
                .completionDate(null)
                .build();

        TodoDTO outputTodo = TodoDTO.builder()
                .id(101L)
                .description("New todo")
                .dueDate(inputTodo.getDueDate())
                .checkMark(false)
                .completionDate(null)
                .build();

        when(todoService.createTodo(any(TodoDTO.class), any())).thenReturn(outputTodo);

        mockMvc.perform(post("/api/todo/create")
                        .with(user(principal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputTodo)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateTodo_Success() throws Exception {
        AuthenticationUserDetails principal = createPrincipalWithAuthority("UPDATE_TODOS");

        TodoDTO updateTodo = TodoDTO.builder()
                .id(102L)
                .description("Updated todo")
                .dueDate(LocalDate.now().plusDays(2))
                .checkMark(true)
                .completionDate(LocalDate.now())
                .build();

        when(todoService.updateTodo(any(TodoDTO.class), any())).thenReturn(updateTodo);

        mockMvc.perform(put("/api/todo/update")
                        .with(user(principal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTodo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(102)))
                .andExpect(jsonPath("$.description", is("Updated todo")));
    }

    @Test
    void testUpdateTodo_Failure() throws Exception {
        AuthenticationUserDetails principal = createPrincipalWithAuthority("test");

        TodoDTO updateTodo = TodoDTO.builder()
                .id(102L)
                .description("Updated todo")
                .dueDate(LocalDate.now().plusDays(2))
                .checkMark(true)
                .completionDate(LocalDate.now())
                .build();

        when(todoService.updateTodo(any(TodoDTO.class), any())).thenReturn(updateTodo);

        mockMvc.perform(put("/api/todo/update")
                        .with(user(principal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTodo)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteTodos_Success() throws Exception {
        AuthenticationUserDetails principal = createPrincipalWithAuthority("DELETE_TODOS");

        mockMvc.perform(delete("/api/todo/delete")
                        .with(user(principal))
                        .with(csrf())
                        .param("ids", "200", "201")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteTodos_Failure() throws Exception {
        AuthenticationUserDetails principal = createPrincipalWithAuthority("test");

        mockMvc.perform(delete("/api/todo/delete")
                        .with(user(principal))
                        .with(csrf())
                        .param("ids", "200", "201")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}

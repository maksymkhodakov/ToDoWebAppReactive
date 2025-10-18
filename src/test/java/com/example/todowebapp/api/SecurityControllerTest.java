package com.example.todowebapp.api;

import com.example.todowebapp.domain.dto.LoginData;
import com.example.todowebapp.domain.dto.LoginResponseDTO;
import com.example.todowebapp.domain.dto.RegisterData;
import com.example.todowebapp.domain.dto.UserDTO;
import com.example.todowebapp.domain.enumerated.UserRole;
import com.example.todowebapp.security.AuthenticationUserDetails;
import com.example.todowebapp.service.SecureBasicAuthenticationService;
import com.example.todowebapp.service.impl.JwtServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SecurityController.class)
@AutoConfigureMockMvc(addFilters = false)
class SecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecureBasicAuthenticationService secureBasicAuthenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private JwtServiceImpl jwtService;


    @Test
    void testCurrentUser_ReturnsUserDTO() throws Exception {
        AuthenticationUserDetails principal = AuthenticationUserDetails.authBuilder()
                .username("user@example.com")
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

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));

        UserDTO expectedUserDTO = UserDTO.builder()
                .id(principal.getUserId())
                .email(principal.getUsername())
                .userRole(principal.getUserRole())
                .privileges(principal.getAuthorities())
                .build();

        when(secureBasicAuthenticationService.getCurrentUser(any(AuthenticationUserDetails.class)))
                .thenReturn(expectedUserDTO);

        TestingAuthenticationToken authToken = new TestingAuthenticationToken(
                principal, null, principal.getAuthorities());
        authToken.setAuthenticated(true);

        // when/then: викликаємо GET /api/me із встановленим токеном
        mockMvc.perform(get("/api/me")
                        .with(authentication(authToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("user@example.com")))
                .andExpect(jsonPath("$.userRole", is("ROLE_BASIC_USER")));
    }


    @Test
    void testRegistration_Success() throws Exception {
        // given: створюємо payload для реєстрації
        RegisterData registerData = RegisterData.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("secret")
                .userRole(UserRole.ROLE_BASIC_USER)
                .build();

        doNothing().when(secureBasicAuthenticationService).register(any(RegisterData.class));

        // when/then: викликаємо POST /api/register і очікуємо 200 OK
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerData)))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin_Success() throws Exception {
        // given: створюємо payload для логіну
        LoginData loginData = LoginData.builder()
                .email("user@example.com")
                .password("password")
                .build();

        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .token("jwt-token")
                .build();

        when(secureBasicAuthenticationService.login(any(LoginData.class))).thenReturn(loginResponseDTO);

        // when/then: викликаємо POST /api/login та перевіряємо отриманий JWT токен
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("jwt-token")));
    }
}

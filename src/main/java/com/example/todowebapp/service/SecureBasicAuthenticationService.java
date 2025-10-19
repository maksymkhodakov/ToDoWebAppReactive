package com.example.todowebapp.service;

import com.example.todowebapp.domain.dto.*;
import com.example.todowebapp.security.AuthenticationUserDetails;
import reactor.core.publisher.Mono;

public interface SecureBasicAuthenticationService {
    Mono<UserDTO> getCurrentUser(AuthenticationUserDetails principal);
    Mono<Void> register(RegisterData data);
    Mono<LoginResponseDTO> login(LoginData data);
}

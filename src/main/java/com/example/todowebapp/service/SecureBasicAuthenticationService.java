package com.example.todowebapp.service;


import com.example.todowebapp.domain.dto.LoginData;
import com.example.todowebapp.domain.dto.LoginResponseDTO;
import com.example.todowebapp.domain.dto.RegisterData;
import com.example.todowebapp.domain.dto.UserDTO;
import com.example.todowebapp.security.AuthenticationUserDetails;

public interface SecureBasicAuthenticationService {
    UserDTO getCurrentUser(AuthenticationUserDetails authenticationUserDetails);
    void register(RegisterData data);
    LoginResponseDTO login(LoginData data);
}

package com.example.todowebapp.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String getEmailFromToken(String token);
    Claims getAllInfoFromToken(String token);
    String generateToken(UserDetails userDetails);
    boolean isValidToken(String token);
}

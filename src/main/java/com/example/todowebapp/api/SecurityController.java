package com.example.todowebapp.api;

import com.example.todowebapp.domain.dto.LoginData;
import com.example.todowebapp.domain.dto.LoginResponseDTO;
import com.example.todowebapp.domain.dto.RegisterData;
import com.example.todowebapp.domain.dto.UserDTO;
import com.example.todowebapp.security.AuthenticationUserDetails;
import com.example.todowebapp.service.SecureBasicAuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SecurityController {

    private final SecureBasicAuthenticationService secureBasicAuthenticationService;

    @GetMapping("/me")
    public Mono<UserDTO> currentUser(@AuthenticationPrincipal AuthenticationUserDetails principal) {
        return secureBasicAuthenticationService.getCurrentUser(principal);
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<Void>> registration(@RequestBody @Valid Mono<RegisterData> body) {
        return body
                .flatMap(secureBasicAuthenticationService::register)
                .thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/login")
    public Mono<LoginResponseDTO> login(@RequestBody @Valid Mono<LoginData> body) {
        return body.flatMap(secureBasicAuthenticationService::login);
    }
}

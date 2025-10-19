package com.example.todowebapp.service.impl;

import com.example.todowebapp.domain.dto.*;
import com.example.todowebapp.domain.entity.User;
import com.example.todowebapp.domain.enumerated.UserRole;
import com.example.todowebapp.exceptions.ApiException;
import com.example.todowebapp.exceptions.ErrorCode;
import com.example.todowebapp.repository.RoleRepository;
import com.example.todowebapp.repository.UserRepository;
import com.example.todowebapp.security.AuthenticationUserDetails;
import com.example.todowebapp.service.JwtService;
import com.example.todowebapp.service.SecureBasicAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecureBasicAuthenticationServiceImpl implements SecureBasicAuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ReactiveUserDetailsService userDetailsService;
    private final ReactiveAuthenticationManager authManager;

    @Override
    public Mono<UserDTO> getCurrentUser(final AuthenticationUserDetails principal) {
        if (principal == null) return Mono.empty();
        return Mono.just(
                UserDTO.builder()
                        .id(principal.getUserId())
                        .email(principal.getUsername())
                        .userRole(principal.getUserRole())
                        .privileges(principal.getAuthorities())
                        .build()
        );
    }

    @Override
    @Transactional
    public Mono<Void> register(final RegisterData data) {
        // 1) validate payload (no admins)
        if (data.getUserRole() == UserRole.ROLE_ADMIN) {
            return Mono.error(new ApiException(ErrorCode.YOU_CANNOT_CREATE_AN_ADMIN_USER));
        }

        // 2) check user uniqueness
        Mono<Void> ensureUnique =
                userRepository.findByEmail(data.getEmail())
                        .flatMap(u -> Mono.<Void>error(new ApiException(ErrorCode.USER_ALREADY_EXISTS)))
                        .switchIfEmpty(Mono.empty());

        // 3) find role, build user with FK, save
        Mono<Void> createUser =
                roleRepository.findByUserRole(data.getUserRole())
                        .switchIfEmpty(Mono.error(new ApiException(ErrorCode.ROLE_NOT_FOUND)))
                        .flatMap(role -> {
                            User user = User.builder()
                                    .name(data.getFirstName())
                                    .lastName(data.getLastName())
                                    .email(data.getEmail())
                                    .password(passwordEncoder.encode(data.getPassword()))
                                    .roleId(role.getId())     // set FK explicitly
                                    .system(false)
                                    .build();
                            return userRepository.save(user).then();
                        });

        return ensureUnique.then(createUser);
    }

    @Override
    public Mono<LoginResponseDTO> login(final LoginData data) {
        // 1) authenticate reactively
        return authManager
                .authenticate(new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword()))
                // 2) load full user details (or reuse auth.getPrincipal())
                .flatMap(auth -> userDetailsService.findByUsername(data.getEmail()))
                // 3) mint JWT
                .map(jwtService::generateToken)
                // 4) build response
                .map(token -> LoginResponseDTO.builder().token(token).build());
    }
}

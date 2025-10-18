package com.example.todowebapp.service.impl;

import com.example.todowebapp.domain.dto.*;
import com.example.todowebapp.domain.entity.Role;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecureBasicAuthenticationServiceImpl implements SecureBasicAuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    @Override
    public UserDTO getCurrentUser(final AuthenticationUserDetails authenticationUserDetails) {
        return authenticationUserDetails == null ? null :
                UserDTO.builder()
                        .id(authenticationUserDetails.getUserId())
                        .email(authenticationUserDetails.getUsername())
                        .userRole(authenticationUserDetails.getUserRole())
                        .privileges(authenticationUserDetails.getAuthorities())
                        .build();
    }

    @Override
    @Transactional
    public void register(final RegisterData data) {
        validateUserPayload(data);

        final Role role = roleRepository.findByUserRole(data.getUserRole())
                .orElseThrow(() -> new ApiException(ErrorCode.ROLE_NOT_FOUND));

        final User user = User.builder()
                .name(data.getFirstName())
                .lastName(data.getLastName())
                .email(data.getEmail())
                .password(passwordEncoder.encode(data.getPassword()))
                .build();

        role.addUser(user);

        roleRepository.save(role);
    }

    private void validateUserPayload(final RegisterData data) {
        if (data.getUserRole() == UserRole.ROLE_ADMIN) {
            throw new ApiException(ErrorCode.YOU_CANNOT_CREATE_AN_ADMIN_USER);
        }

        final Optional<User> userInDb = userRepository.findByEmail(data.getEmail());

        if (userInDb.isPresent())  {
            throw new ApiException(ErrorCode.USER_ALREADY_EXISTS);
        }
    }

    @Override
    public LoginResponseDTO login(final LoginData data) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword()));

        final UserDetails userDetails = userDetailsService.loadUserByUsername(data.getEmail());

        final String token = jwtService.generateToken(userDetails);

        return LoginResponseDTO.builder()
                .token(token)
                .build();
    }
}

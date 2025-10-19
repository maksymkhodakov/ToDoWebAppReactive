package com.example.todowebapp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ReactiveUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Reactive equivalent of DaoAuthenticationProvider + AuthenticationManager.
     * Use this if you have a login flow that checks username/password (e.g. /auth/login)
     * and then you mint a JWT.
     */
    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        var mgr = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        mgr.setPasswordEncoder(passwordEncoder);
        return mgr;
    }
}
